package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.sommeri.less4j.LessCompiler.Configuration;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.DetachedRuleset;
import com.github.sommeri.less4j.core.ast.DetachedRulesetReference;
import com.github.sommeri.less4j.core.ast.EmbeddedScript;
import com.github.sommeri.less4j.core.ast.EscapedSelector;
import com.github.sommeri.less4j.core.ast.EscapedValue;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FaultyNode;
import com.github.sommeri.less4j.core.ast.FixedNamePart;
import com.github.sommeri.less4j.core.ast.GeneralBody;
import com.github.sommeri.less4j.core.ast.IndirectVariable;
import com.github.sommeri.less4j.core.ast.InterpolableName;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.SimpleSelector;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.ast.VariableNamePart;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionsEvaluator;
import com.github.sommeri.less4j.core.compiler.expressions.strings.StringInterpolator;
import com.github.sommeri.less4j.core.compiler.scopes.FullMixinDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.InScopeSnapshotRunner;
import com.github.sommeri.less4j.core.compiler.scopes.InScopeSnapshotRunner.ITask;
import com.github.sommeri.less4j.core.compiler.scopes.IteratedScope;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.utils.CssPrinter;
import com.github.sommeri.less4j.utils.QuotesKeepingInStringCssPrinter;

public class ReferencesSolver {

  public static final String ALL_ARGUMENTS = "@arguments";
  private ASTManipulator manipulator = new ASTManipulator();
  private final MixinsRulesetsSolver mixinsSolver;
  private final ProblemsHandler problemsHandler;
  private final Configuration configuration;
  private final AstNodesStack semiCompiledNodes = new AstNodesStack();
  private final StringInterpolator stringInterpolator;

  public ReferencesSolver(ProblemsHandler problemsHandler, Configuration configuration) {
    this.problemsHandler = problemsHandler;
    this.configuration = configuration;
    this.stringInterpolator = new StringInterpolator(problemsHandler);
    this.mixinsSolver = new MixinsRulesetsSolver(this, semiCompiledNodes, problemsHandler, configuration);
  }

  public void solveReferences(final ASTCssNode node, final IScope scope) {
    doSolveReferences(node, new IteratedScope(scope));
  }

  private void doSolveReferences(final ASTCssNode node, final IteratedScope scope) {
    // ... and I'm starting to see the point of closures ...
    InScopeSnapshotRunner.runInLocalDataSnapshot(scope, new ITask() {

      @Override
      public void run() {
        unsafeDoSolveReferences(node, scope);
      }

    });
  }

  protected void unsafeDoSolveReferences(ASTCssNode node, IScope scope) {
    unsafeDoSolveReferences(node, new IteratedScope(scope));
  }

  private void unsafeDoSolveReferences(ASTCssNode node, IteratedScope iteratedScope) {
    // The stack of nodes under compilation is necessary to prevent 
    // cycling. The cycling is possible if two namespaces reference 
    // each other and therefore each effectively requires compiled 
    // version of itself 
    semiCompiledNodes.push(node);

    try {
      List<ASTCssNode> childs = new ArrayList<ASTCssNode>(node.getChilds());
      if (!childs.isEmpty()) {
        IScope scope = iteratedScope.getScope();

        // solve all detached ruleset/mixin references and store solutions
        Map<ASTCssNode, GeneralBody> solvedReferences = solveCalls(childs, scope);

        // solve whatever is not a mixin/detached ruleset reference
        solveNonCalligReferences(childs, iteratedScope);

        // replace mixin references by their solutions - we need to do it in the end
        // the scope and ast would get out of sync otherwise
        replaceMixinReferences(solvedReferences);
      }
    } finally {
      semiCompiledNodes.pop();
    }
  }

  private void solveNonCalligReferences(List<ASTCssNode> childs, IteratedScope iteratedScope) {
    ExpressionsEvaluator cssGuardsValidator = new ExpressionsEvaluator(iteratedScope.getScope(), problemsHandler, configuration);
    for (ASTCssNode kid : childs) {
      if (isMixinReference(kid) || isDetachedRulesetReference(kid))
        continue;

      if (isRuleset(kid)) {
        RuleSet ruleSet = (RuleSet) kid;
        if (cssGuardsValidator.guardsSatisfied(ruleSet)) {
          ruleSet.removeGuards();
        } else {
          manipulator.removeFromClosestBody(ruleSet);
          //skip child scope
          iteratedScope.getNextChild();
          continue;
        }
      }

      if (AstLogic.isQuotelessUrlFunction(kid)) {
        continue;
      }

      if (AstLogic.hasOwnScope(kid)) {
        IteratedScope scope = iteratedScope.getNextChild();
        doSolveReferences(kid, scope);
      } else {
        boolean finishedNode = solveIfVariableReference(kid, iteratedScope.getScope());
        if (!finishedNode)
          unsafeDoSolveReferences(kid, iteratedScope);
      }
    }
  }

  private boolean isMixinReference(ASTCssNode kid) {
    return kid.getType() == ASTCssNodeType.MIXIN_REFERENCE;
  }

  private boolean isDetachedRulesetReference(ASTCssNode kid) {
    return kid.getType() == ASTCssNodeType.DETACHED_RULESET_REFERENCE;
  }

  private boolean isRuleset(ASTCssNode kid) {
    return kid.getType() == ASTCssNodeType.RULE_SET;
  }

  private void replaceMixinReferences(Map<ASTCssNode, GeneralBody> solvedReferences) {
    for (Entry<ASTCssNode, GeneralBody> entry : solvedReferences.entrySet()) {
      ASTCssNode mixinReference = entry.getKey();
      GeneralBody replacement = entry.getValue();

      manipulator.setTreeSilentness(replacement, mixinReference.isSilent());
      manipulator.replaceInBody(mixinReference, replacement.getMembers());
    }
  }

  private Map<ASTCssNode, GeneralBody> solveCalls(List<ASTCssNode> childs, IScope referenceScope) {
    Map<ASTCssNode, GeneralBody> solvedMixinReferences = new HashMap<ASTCssNode, GeneralBody>();
    for (ASTCssNode kid : childs) {
      if (isMixinReference(kid)) {
        MixinReference reference = (MixinReference) kid;

        List<FullMixinDefinition> foundMixins = findReferencedMixins(reference, referenceScope);
        GeneralBody replacement = mixinsSolver.buildMixinReferenceReplacement(reference, referenceScope, foundMixins);

        AstLogic.validateLessBodyCompatibility(reference, replacement.getMembers(), problemsHandler);
        solvedMixinReferences.put(reference, replacement);
      } else if (isDetachedRulesetReference(kid)) {
        DetachedRulesetReference detachedRulesetReference = (DetachedRulesetReference) kid;
        Expression fullNodeDefinition = referenceScope.getValue(detachedRulesetReference.getVariable());
        
        if (fullNodeDefinition == null) {
          handleUnavailableDetachedRulesetReference(detachedRulesetReference, solvedMixinReferences);
        } else {
          ExpressionsEvaluator expressionEvaluator = new ExpressionsEvaluator(referenceScope, problemsHandler, configuration);
          Expression evaluatedDetachedRuleset = expressionEvaluator.evaluate(fullNodeDefinition);
          fullNodeDefinition = evaluatedDetachedRuleset;
          if (evaluatedDetachedRuleset.getType() != ASTCssNodeType.DETACHED_RULESET) {
            handleWrongDetachedRulesetReference(detachedRulesetReference, evaluatedDetachedRuleset, solvedMixinReferences);
          } else {
            DetachedRuleset detachedRuleset = (DetachedRuleset) evaluatedDetachedRuleset;
            IScope scope = detachedRuleset.getScope();
            GeneralBody replacement = mixinsSolver.buildDetachedRulesetReplacement(detachedRulesetReference, referenceScope, detachedRuleset, scope);
            AstLogic.validateLessBodyCompatibility(kid, replacement.getMembers(), problemsHandler);
            solvedMixinReferences.put(kid, replacement);
          }
        }

      }

    }
    return solvedMixinReferences;
  }

  private void handleUnavailableDetachedRulesetReference(DetachedRulesetReference detachedRulesetReference, Map<ASTCssNode, GeneralBody> solvedReferences) {
    problemsHandler.detachedRulesetNotfound(detachedRulesetReference);
    GeneralBody errorBody = new GeneralBody(detachedRulesetReference.getUnderlyingStructure());
    errorBody.addMember(new FaultyNode(detachedRulesetReference));
    solvedReferences.put(detachedRulesetReference, errorBody);
  }

  private void handleWrongDetachedRulesetReference(DetachedRulesetReference detachedRulesetReference, Expression value,  Map<ASTCssNode, GeneralBody> solvedReferences) {
    problemsHandler.wrongDetachedRulesetReference(detachedRulesetReference, value);
    GeneralBody errorBody = new GeneralBody(detachedRulesetReference.getUnderlyingStructure());
    errorBody.addMember(new FaultyNode(detachedRulesetReference));
    solvedReferences.put(detachedRulesetReference, errorBody);
  }

  protected List<FullMixinDefinition> findReferencedMixins(MixinReference mixinReference, IScope scope) {
    MixinReferenceFinder finder = new MixinReferenceFinder(this, semiCompiledNodes);
    List<FullMixinDefinition> sameNameMixins = finder.getNearestMixins(scope, mixinReference);
    if (sameNameMixins.isEmpty()) {
      //error reporting
      if (!finder.foundNamespace())
        problemsHandler.undefinedNamespace(mixinReference);

      problemsHandler.undefinedMixin(mixinReference);
      return new ArrayList<FullMixinDefinition>();
    }

    MixinsReferenceMatcher matcher = new MixinsReferenceMatcher(scope, problemsHandler, configuration);
    List<FullMixinDefinition> mixins = matcher.filterByParametersNumber(mixinReference, sameNameMixins);
    if (mixins.isEmpty()) {
      problemsHandler.noMixinHasRightParametersCountError(mixinReference);
      return mixins;
    }
    mixins = matcher.filterByPatterns(mixinReference, mixins);
    if (mixins.isEmpty())
      problemsHandler.patternsInMatchingMixinsDoNotMatch(mixinReference);

    return mixins;
  }

  private boolean solveIfVariableReference(ASTCssNode node, IScope scope) {
    ExpressionsEvaluator expressionEvaluator = new ExpressionsEvaluator(scope, problemsHandler, configuration);
    switch (node.getType()) {
    case VARIABLE: {
      Expression replacement = expressionEvaluator.evaluate((Variable) node);
      manipulator.replaceAndSynchronizeSilentness(node, replacement);
      return true;
    }
    case INDIRECT_VARIABLE: {
      Expression replacement = expressionEvaluator.evaluate((IndirectVariable) node);
      manipulator.replaceAndSynchronizeSilentness(node, replacement);
      return true;
    }
    case STRING_EXPRESSION: {
      Expression replacement = expressionEvaluator.evaluate((CssString) node);
      manipulator.replaceAndSynchronizeSilentness(node, replacement);
      return true;
    }
    case ESCAPED_VALUE: {
      Expression replacement = expressionEvaluator.evaluate((EscapedValue) node);
      manipulator.replaceAndSynchronizeSilentness(node, replacement);
      return true;
    }
    case EMBEDDED_SCRIPT: {
      Expression replacement = expressionEvaluator.evaluate((EmbeddedScript) node);
      manipulator.replaceAndSynchronizeSilentness(node, replacement);
      return true;
    }
    case ESCAPED_SELECTOR: {
      SimpleSelector replacement = interpolateEscapedSelector((EscapedSelector) node, expressionEvaluator);
      manipulator.replaceAndSynchronizeSilentness(node, replacement);
      return true;
    }
    case FIXED_NAME_PART: {
      FixedNamePart part = (FixedNamePart) node;
      FixedNamePart replacement = interpolateFixedNamePart(part, expressionEvaluator);
      manipulator.replaceMemberAndSynchronizeSilentness(part, replacement);
      return true;
    }
    case VARIABLE_NAME_PART: {
      VariableNamePart part = (VariableNamePart) node;
      Expression value = expressionEvaluator.evaluate(part.getVariable());
      FixedNamePart fixedName = toFixedName(value, node.getUnderlyingStructure(), part);
      FixedNamePart replacement = interpolateFixedNamePart(fixedName, expressionEvaluator);
      manipulator.replaceMemberAndSynchronizeSilentness(part, replacement);
      return true;
    }
    default: // nothing
    }
    return false;
  }

  private FixedNamePart toFixedName(Expression value, HiddenTokenAwareTree parent, VariableNamePart part) {
    CssPrinter printer = new QuotesKeepingInStringCssPrinter();
    printer.append(value);
    // property based alternative would be nice, but does not seem to be needed
    FixedNamePart fixedName = new FixedNamePart(parent, printer.toString());
    return fixedName;
  }

  private SimpleSelector interpolateEscapedSelector(EscapedSelector input, ExpressionsEvaluator expressionEvaluator) {
    HiddenTokenAwareTree underlying = input.getUnderlyingStructure();
    String value = stringInterpolator.replaceIn(input.getValue(), expressionEvaluator, input.getUnderlyingStructure());
    InterpolableName interpolableName = new InterpolableName(underlying, new FixedNamePart(underlying, value));
    return new SimpleSelector(input.getUnderlyingStructure(), input.getLeadingCombinator(), interpolableName, false);
  }

  private FixedNamePart interpolateFixedNamePart(FixedNamePart input, ExpressionsEvaluator expressionEvaluator) {
    String value = stringInterpolator.replaceIn(input.getName(), expressionEvaluator, input.getUnderlyingStructure());
    return new FixedNamePart(input.getUnderlyingStructure(), value);
  }

}
