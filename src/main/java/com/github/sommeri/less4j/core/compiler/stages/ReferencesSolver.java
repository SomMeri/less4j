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
import com.github.sommeri.less4j.core.ast.EmbeddedScript;
import com.github.sommeri.less4j.core.ast.EscapedSelector;
import com.github.sommeri.less4j.core.ast.EscapedValue;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FixedNamePart;
import com.github.sommeri.less4j.core.ast.GeneralBody;
import com.github.sommeri.less4j.core.ast.IndirectVariable;
import com.github.sommeri.less4j.core.ast.InterpolableName;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.SimpleSelector;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.ast.VariableNamePart;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionEvaluator;
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
  private final MixinsSolver mixinsSolver;
  private final ProblemsHandler problemsHandler;
  private final Configuration configuration;
  private final AstNodesStack semiCompiledNodes = new AstNodesStack();
  private final StringInterpolator stringInterpolator;

  public ReferencesSolver(ProblemsHandler problemsHandler, Configuration configuration) {
    this.problemsHandler = problemsHandler;
    this.configuration = configuration;
    this.stringInterpolator = new StringInterpolator(problemsHandler);
    this.mixinsSolver = new MixinsSolver(this, semiCompiledNodes, problemsHandler, configuration);
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
        Map<MixinReference, GeneralBody> solvedMixinReferences = solveCalls(childs, scope);

        // solve whatever is not a mixin/detached ruleset reference
        solveNonCalligReferences(childs, iteratedScope);

        // replace mixin references by their solutions - we need to do it in the end
        // the scope and ast would get out of sync otherwise
        replaceMixinReferences(solvedMixinReferences);
      }
    } finally {
      semiCompiledNodes.pop();
    }
  }

  private void solveNonCalligReferences(List<ASTCssNode> childs, IteratedScope iteratedScope) {
    ExpressionEvaluator cssGuardsValidator = new ExpressionEvaluator(iteratedScope.getScope(), problemsHandler, configuration);
    for (ASTCssNode kid : childs) {
      if (isMixinReference(kid))
        continue;

      if (isRuleset(kid)) {
        RuleSet ruleSet = (RuleSet) kid;
        if (cssGuardsValidator.guardsSatisfied(ruleSet)) {
          ruleSet.removeGuards();
        } else {
          manipulator.removeFromClosestBody(ruleSet);
          //skip child scope
          iteratedScope.getNextChild();
          continue ;
        }
      }
      
      if (AstLogic.isQuotelessUrlFunction(kid)) {
        continue ;
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
  
  @SuppressWarnings("unused")
  private boolean isDetachedRulesetReference(ASTCssNode kid) {
    return kid.getType() == ASTCssNodeType.DETACHED_RULESET_REFERENCE;
  }

  private boolean isRuleset(ASTCssNode kid) {
    return kid.getType() == ASTCssNodeType.RULE_SET;
  }

  private void replaceMixinReferences(Map<MixinReference, GeneralBody> solvedMixinReferences) {
    for (Entry<MixinReference, GeneralBody> entry : solvedMixinReferences.entrySet()) {
      MixinReference mixinReference = entry.getKey();
      GeneralBody replacement = entry.getValue();
      
      manipulator.setTreeSilentness(replacement, mixinReference.isSilent());
      manipulator.replaceInBody(mixinReference, replacement.getMembers());
    }
  }

  private Map<MixinReference, GeneralBody> solveCalls(List<ASTCssNode> childs, IScope mixinReferenceScope) {
    Map<MixinReference, GeneralBody> solvedMixinReferences = new HashMap<MixinReference, GeneralBody>();
    for (ASTCssNode kid : childs) {
      if (isMixinReference(kid)) {
        MixinReference mixinReference = (MixinReference) kid;

        List<FullMixinDefinition> foundMixins = findReferencedMixins(mixinReference, mixinReferenceScope);
        GeneralBody replacement = mixinsSolver.buildMixinReferenceReplacement(mixinReference, mixinReferenceScope, foundMixins);

        AstLogic.validateLessBodyCompatibility(mixinReference, replacement.getMembers(), problemsHandler);
        solvedMixinReferences.put(mixinReference, replacement);
      }
    }
    return solvedMixinReferences;
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
    ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(scope, problemsHandler, configuration);
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

  private SimpleSelector interpolateEscapedSelector(EscapedSelector input, ExpressionEvaluator expressionEvaluator) {
    HiddenTokenAwareTree underlying = input.getUnderlyingStructure();
    String value = stringInterpolator.replaceIn(input.getValue(), expressionEvaluator, input.getUnderlyingStructure());
    InterpolableName interpolableName = new InterpolableName(underlying, new FixedNamePart(underlying, value));
    return new SimpleSelector(input.getUnderlyingStructure(), input.getLeadingCombinator(), interpolableName, false);
  }

  private FixedNamePart interpolateFixedNamePart(FixedNamePart input, ExpressionEvaluator expressionEvaluator) {
    String value = stringInterpolator.replaceIn(input.getName(), expressionEvaluator, input.getUnderlyingStructure());
    return new FixedNamePart(input.getUnderlyingStructure(), value);
  }

}
