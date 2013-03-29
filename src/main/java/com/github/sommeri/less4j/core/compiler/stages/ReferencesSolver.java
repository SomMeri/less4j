package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.ArgumentDeclaration;
import com.github.sommeri.less4j.core.ast.Body;
import com.github.sommeri.less4j.core.ast.BodyOwner;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.Declaration;
import com.github.sommeri.less4j.core.ast.EscapedSelector;
import com.github.sommeri.less4j.core.ast.EscapedValue;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FixedNamePart;
import com.github.sommeri.less4j.core.ast.GeneralBody;
import com.github.sommeri.less4j.core.ast.IndirectVariable;
import com.github.sommeri.less4j.core.ast.InterpolableName;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.SimpleSelector;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.ast.VariableNamePart;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionEvaluator;
import com.github.sommeri.less4j.core.compiler.expressions.strings.StringInterpolator;
import com.github.sommeri.less4j.core.compiler.scopes.FullMixinDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.IteratedScope;
import com.github.sommeri.less4j.core.compiler.scopes.Scope;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.utils.ArraysUtils;
import com.github.sommeri.less4j.utils.QuotesKeepingInStringCssPrinter;

public class ReferencesSolver {

  public static final String ALL_ARGUMENTS = "@arguments";
  private ASTManipulator manipulator = new ASTManipulator();
  private final ProblemsHandler problemsHandler;
  private StringInterpolator stringInterpolator = new StringInterpolator();

  public ReferencesSolver(ProblemsHandler problemsHandler) {
    this.problemsHandler = problemsHandler;
  }

  public void solveReferences(ASTCssNode node, Scope scope) {
    doSolveReferences(node, new IteratedScope(scope));
  }

  private void doSolveReferences(ASTCssNode node, Scope scope) {
    doSolveReferences(node, new IteratedScope(scope));
  }

  private void doSolveReferences(ASTCssNode node, IteratedScope iteratedScope) {
    boolean finishedNode = replaceIfVariableReference(node, iteratedScope);

    if (!finishedNode) {
      List<ASTCssNode> childs = new ArrayList<ASTCssNode>(node.getChilds());
      // solve all mixin references and store solutions
      Map<MixinReference, GeneralBody> solvedMixinReferences = solveMixinReferences(childs, iteratedScope);
      // solve whatever is not a mixin reference
      solveNonMixinReferences(childs, iteratedScope);
      // replace mixin references by their solutions
      replaceMixinReferences(solvedMixinReferences);
    }

  }

  private void solveNonMixinReferences(List<ASTCssNode> childs, IteratedScope iteratedScope) {
    for (ASTCssNode kid : childs) {
      if (kid.getType() != ASTCssNodeType.MIXIN_REFERENCE) {
        if (AstLogic.hasOwnScope(kid)) {
          doSolveReferences(kid, iteratedScope.getNextChild());
        } else {
          doSolveReferences(kid, iteratedScope);
        }
      }
    }
  }

  private void replaceMixinReferences(Map<MixinReference, GeneralBody> solvedMixinReferences) {
    for (Entry<MixinReference, GeneralBody> entry : solvedMixinReferences.entrySet()) {
      MixinReference mixinReference = entry.getKey();
      GeneralBody replacement = entry.getValue();
      manipulator.replaceInBody(mixinReference, replacement.getMembers());
    }
  }

  private Map<MixinReference, GeneralBody> solveMixinReferences(List<ASTCssNode> childs, IteratedScope iteratedScope) {
    Map<MixinReference, GeneralBody> solvedMixinReferences = new HashMap<MixinReference, GeneralBody>();
    for (ASTCssNode kid : childs) {
      if (kid.getType() == ASTCssNodeType.MIXIN_REFERENCE) {
        MixinReference mixinReference = (MixinReference) kid;
        GeneralBody replacement = resolveMixinReference(mixinReference, iteratedScope.getScope());
        AstLogic.validateLessBodyCompatibility(mixinReference, replacement.getMembers(), problemsHandler);
        
        solvedMixinReferences.put(mixinReference, replacement);
      }
    }
    return solvedMixinReferences;
  }

  private boolean replaceIfVariableReference(ASTCssNode node, IteratedScope iteratedScope) {
    ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(iteratedScope.getScope(), problemsHandler);
    switch (node.getType()) {
    case VARIABLE: {
      Expression replacement = expressionEvaluator.evaluate((Variable) node);
      manipulator.replace(node, replacement);
      return true;
    }
    case INDIRECT_VARIABLE: {
      Expression replacement = expressionEvaluator.evaluate((IndirectVariable) node);
      manipulator.replace(node, replacement);
      return true;
    }
    case STRING_EXPRESSION: {
      Expression replacement = expressionEvaluator.evaluate((CssString) node);
      manipulator.replace(node, replacement);
      return true;
    }
    case ESCAPED_VALUE: {
      Expression replacement = expressionEvaluator.evaluate((EscapedValue) node);
      manipulator.replace(node, replacement);
      return true;
    }
    case ESCAPED_SELECTOR: {
      SimpleSelector replacement = interpolateEscapedSelector((EscapedSelector) node, expressionEvaluator);
      manipulator.replace(node, replacement);
      return true;
    }
    case FIXED_NAME_PART: {
      FixedNamePart part = (FixedNamePart) node;
      FixedNamePart replacement = interpolateFixedNamePart(part, expressionEvaluator);
      part.getParent().replaceMember(part, replacement);
      return true;
    }
    case VARIABLE_NAME_PART: {
      VariableNamePart part = (VariableNamePart) node;
      Expression value = expressionEvaluator.evaluate(part.getVariable());
      FixedNamePart fixedName = toFixedName(value, node.getUnderlyingStructure());
      part.getParent().replaceMember(part, interpolateFixedNamePart(fixedName, expressionEvaluator));
      return true;
    }
    }
    return false;
  }

  private FixedNamePart toFixedName(Expression value, HiddenTokenAwareTree parent) {
    QuotesKeepingInStringCssPrinter printer = new QuotesKeepingInStringCssPrinter();
    printer.append(value);
    // property based alternative would be nice, but does not seem to be needed
    FixedNamePart fixedName = new FixedNamePart(parent, printer.toString());
    return fixedName;
  }

  private SimpleSelector interpolateEscapedSelector(EscapedSelector input, ExpressionEvaluator expressionEvaluator) {
    HiddenTokenAwareTree underlying = input.getUnderlyingStructure();
    String value = stringInterpolator.replaceIn(input.getValue(), expressionEvaluator, input.getUnderlyingStructure());
    InterpolableName interpolableName = new InterpolableName(underlying, new FixedNamePart(underlying, value));
    return new SimpleSelector(input.getUnderlyingStructure(), interpolableName, false);
  }

  private FixedNamePart interpolateFixedNamePart(FixedNamePart input, ExpressionEvaluator expressionEvaluator) {
    String value = stringInterpolator.replaceIn(input.getName(), expressionEvaluator, input.getUnderlyingStructure());
    return new FixedNamePart(input.getUnderlyingStructure(), value);
  }

  private GeneralBody resolveMixinReference(MixinReference reference, Scope scope) {
    List<FullMixinDefinition> sameNameMixins = scope.getNearestMixins(reference, problemsHandler);
    return resolveReferencedMixins(reference, scope, sameNameMixins);
  }

  private GeneralBody resolveReferencedMixins(MixinReference reference, Scope referenceScope, List<FullMixinDefinition> sameNameMixins) {
    if (sameNameMixins.isEmpty())
      problemsHandler.undefinedMixin(reference);

    List<FullMixinDefinition> mixins = (new MixinsReferenceMatcher(referenceScope, problemsHandler)).filter(reference, sameNameMixins);
    if (mixins.isEmpty())
      problemsHandler.unmatchedMixin(reference);

    GeneralBody result = new GeneralBody(reference.getUnderlyingStructure());
    for (FullMixinDefinition fullMixin : mixins) {
      Scope combinedScope = calculateMixinsOwnVariables(reference, referenceScope, fullMixin);
      ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(combinedScope, problemsHandler);

      ReusableStructure mixin = fullMixin.getMixin();
      if (expressionEvaluator.evaluate(mixin.getGuards())) {
        GeneralBody body = mixin.getBody().clone();
        doSolveReferences(body, combinedScope);
        result.addMembers(body.getMembers());

        Scope returnValues = expressionEvaluator.evaluateValues(combinedScope);
        referenceScope.addToPlaceholder(returnValues);
      }
    }

    referenceScope.closePlaceholder();
    resolveImportance(reference, result);
    shiftComments(reference, result);

    return result;
  }

  private void shiftComments(MixinReference reference, GeneralBody result) {
    List<ASTCssNode> childs = result.getMembers();
    if (!childs.isEmpty()) {
      childs.get(0).addOpeningComments(reference.getOpeningComments());
      childs.get(childs.size() - 1).addTrailingComments(reference.getTrailingComments());
    }
  }

  private void resolveImportance(MixinReference reference, GeneralBody result) {
    if (reference.isImportant()) {
      declarationsAreImportant(result);
    }
  }

  @SuppressWarnings("rawtypes")
  private void declarationsAreImportant(Body result) {
    for (ASTCssNode kid : result.getMembers()) {
      if (kid instanceof Declaration) {
        Declaration declaration = (Declaration) kid;
        declaration.setImportant(true);
      } else if (kid instanceof BodyOwner<?>) {
        BodyOwner owner = (BodyOwner) kid;
        declarationsAreImportant(owner.getBody());
      }
    }
  }

  private Scope calculateMixinsOwnVariables(MixinReference reference, Scope referenceScope, FullMixinDefinition mixin) {
    Scope joinScopes = joinScopes(mixin.getScope(), buildMixinsArgumentsScope(reference, referenceScope, mixin), referenceScope);
    return joinScopes;
  }

  public static Scope joinScopes(Scope mixinsScope, Scope arguments, Scope callerScope) {
    Scope result = mixinsScope.copyWithChildChain(arguments);
    Scope mixinsScopeParent = mixinsScope.getParent();
    if (mixinsScopeParent != null) {
      arguments.setParent(mixinsScopeParent.copyWithParentsChain());
    }
    Scope rootOfTheMixinsScope = result.getRootScope();
    rootOfTheMixinsScope.setParent(callerScope.copyWithParentsChain());
    return result;
  }

  private Scope buildMixinsArgumentsScope(MixinReference reference, Scope referenceScope, FullMixinDefinition mixin) {
    ArgumentsBuilder builder = new ArgumentsBuilder(reference, mixin.getMixin(), new ExpressionEvaluator(referenceScope, problemsHandler), problemsHandler);
    return builder.build();
  }

}

class ArgumentsBuilder {

  // utils
  private final ProblemsHandler problemsHandler;
  private final ExpressionEvaluator referenceEvaluator;
  private final String ALL_ARGUMENTS = ReferencesSolver.ALL_ARGUMENTS;

  // input
  private Iterator<Expression> positionalParameters;
  private ReusableStructure mixin;
  private MixinReference reference;

  // results
  private List<Expression> allValues = new ArrayList<Expression>();
  private Scope argumentsScope;

  public ArgumentsBuilder(MixinReference reference, ReusableStructure pureMixin, ExpressionEvaluator referenceEvaluator, ProblemsHandler problemsHandler) {
    super();
    this.referenceEvaluator = referenceEvaluator;
    this.problemsHandler = problemsHandler;
    positionalParameters = reference.getPositionalParameters().iterator();
    argumentsScope = Scope.createScope(reference, "#arguments-" + reference + "#", null);
    mixin = pureMixin;
    this.reference = reference;
  }

  public Scope build() {
    int length = mixin.getParameters().size();
    for (int i = 0; i < length; i++) {
      ASTCssNode parameter = mixin.getParameters().get(i);
      if (parameter.getType() == ASTCssNodeType.ARGUMENT_DECLARATION) {
        add((ArgumentDeclaration) parameter);
      } else {
        skipPositionalParameter();
      }

    }

    Expression compoundedValues = referenceEvaluator.joinAll(allValues, reference);
    argumentsScope.registerVariableIfNotPresent(ALL_ARGUMENTS, compoundedValues);
    return argumentsScope;
  }

  private void skipPositionalParameter() {
    positionalParameters.next();
  }

  private void add(ArgumentDeclaration declaration) {
    if (canFillFromNamed(declaration)) {
      fillFromNamed(declaration);
    } else if (declaration.isCollector()) {
      addAsCollector(declaration);
    } else if (canFillFromPositional()) {
      fillFromPositional(declaration);
    } else if (hasDefault(declaration)) {
      fillFromDefault(declaration);
    } else {
      if (declaration.getValue() == null)
        problemsHandler.undefinedMixinParameterValue(mixin, declaration, reference);
    }

  }

  private void fillFromNamed(ArgumentDeclaration declaration) {
    Expression value = referenceEvaluator.evaluate(reference.getNamedParameter(declaration.getVariable()));
    allValues.add(value);
    argumentsScope.registerVariable(declaration, value);
  }

  private boolean canFillFromNamed(ArgumentDeclaration declaration) {
    return reference.hasNamedParameter(declaration.getVariable());
  }

  private void fillFromDefault(ArgumentDeclaration declaration) {
    allValues.add(declaration.getValue());
    argumentsScope.registerVariable(declaration);
  }

  private boolean hasDefault(ArgumentDeclaration declaration) {
    return declaration.getValue() != null;
  }

  private void fillFromPositional(ArgumentDeclaration declaration) {
    Expression value = referenceEvaluator.evaluate(positionalParameters.next());
    allValues.add(value);
    argumentsScope.registerVariable(declaration, value);
  }

  private boolean canFillFromPositional() {
    return positionalParameters.hasNext();
  }

  private void addAsCollector(ArgumentDeclaration declaration) {
    List<Expression> allArgumentsFrom = referenceEvaluator.evaluateAll(ArraysUtils.remaining(positionalParameters));
    allValues.addAll(allArgumentsFrom);
    Expression value = referenceEvaluator.joinAll(allArgumentsFrom, reference);
    argumentsScope.registerVariable(declaration, value);

  }

}
