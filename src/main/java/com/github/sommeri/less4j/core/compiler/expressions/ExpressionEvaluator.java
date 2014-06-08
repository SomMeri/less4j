package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.EmbeddedLessGenerator;
import com.github.sommeri.less4j.EmbeddedScriptGenerator;
import com.github.sommeri.less4j.LessCompiler.Configuration;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.AbstractVariableDeclaration;
import com.github.sommeri.less4j.core.ast.BinaryExpression;
import com.github.sommeri.less4j.core.ast.BinaryExpressionOperator.Operator;
import com.github.sommeri.less4j.core.ast.ComparisonExpression;
import com.github.sommeri.less4j.core.ast.ComparisonExpressionOperator;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.EmbeddedScript;
import com.github.sommeri.less4j.core.ast.EscapedValue;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FaultyExpression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.Guard;
import com.github.sommeri.less4j.core.ast.GuardCondition;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.ast.IndirectVariable;
import com.github.sommeri.less4j.core.ast.ListExpression;
import com.github.sommeri.less4j.core.ast.ListExpressionOperator;
import com.github.sommeri.less4j.core.ast.NamedExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression.Dimension;
import com.github.sommeri.less4j.core.ast.ParenthesesExpression;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.ReusableStructureName;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.SignedExpression;
import com.github.sommeri.less4j.core.ast.SignedExpression.Sign;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.compiler.expressions.strings.StringInterpolator;
import com.github.sommeri.less4j.core.compiler.scopes.BasicScope;
import com.github.sommeri.less4j.core.compiler.scopes.FullExpressionDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.FullMixinDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.ILocalScope;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.ScopesTree;
import com.github.sommeri.less4j.core.compiler.scopes.local.LocalScope;
import com.github.sommeri.less4j.core.problems.BugHappened;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class ExpressionEvaluator {

  private VariableCycleDetector cycleDetector = new VariableCycleDetector();
  private final IScope scope;
  private final ProblemsHandler problemsHandler;
  private ArithmeticCalculator arithmeticCalculator;
  private ColorsCalculator colorsCalculator;
  private ExpressionComparator comparator = new GuardsComparator();
  private List<FunctionsPackage> functions = new ArrayList<FunctionsPackage>();
  private StringInterpolator stringInterpolator;
  private StringInterpolator embeddedScriptInterpolator;
  private EmbeddedScriptGenerator embeddedScripting;
  
  public ExpressionEvaluator(ProblemsHandler problemsHandler, Configuration configuration) {
    this(new NullScope(), problemsHandler, configuration);
  }

  public ExpressionEvaluator(IScope scope, ProblemsHandler problemsHandler, Configuration configuration) {
    super();
    this.scope = scope == null ? new NullScope() : scope;
    this.problemsHandler = problemsHandler;
    arithmeticCalculator = new ArithmeticCalculator(problemsHandler);
    colorsCalculator = new ColorsCalculator(problemsHandler);
    embeddedScripting = configuration.getEmbeddedScriptGenerator() == null ? new EmbeddedLessGenerator() : configuration.getEmbeddedScriptGenerator();
    stringInterpolator = new StringInterpolator(problemsHandler);
    embeddedScriptInterpolator = new StringInterpolator(embeddedScripting, problemsHandler);
    
    functions.add(new CustomFunctions(problemsHandler, configuration.getCustomFunctions()));
    functions.add(new MathFunctions(problemsHandler));
    functions.add(new StringFunctions(problemsHandler));
    functions.add(new ColorFunctions(problemsHandler));
    functions.add(new MiscFunctions(problemsHandler));
    functions.add(new EmbeddedScriptFunctions(problemsHandler));
    functions.add(new TypeFunctions(problemsHandler));
  }
  
  protected void addFunctionsPack(FunctionsPackage pack){
    functions.add(pack);
  }

  public Expression joinAll(List<Expression> allArguments, ASTCssNode parent) {
    if (allArguments.isEmpty())
      return new IdentifierExpression(parent.getUnderlyingStructure(), "");
    
    return new ListExpression(parent.getUnderlyingStructure(), allArguments, new ListExpressionOperator(parent.getUnderlyingStructure(), ListExpressionOperator.Operator.EMPTY_OPERATOR));
  }

  public List<Expression> evaluateAll(List<Expression> expressions) {
    List<Expression> values = new ArrayList<Expression>();
    for (Expression argument : expressions) {
      values.add(evaluate(argument));
    }

    return values;
  }

  public Expression evaluate(CssString input) {
    String value = stringInterpolator.replaceIn(input.getValue(), this, input.getUnderlyingStructure());
    return new CssString(input.getUnderlyingStructure(), value, input.getQuoteType());
  }
  
  public Expression evaluate(EscapedValue input) {
    String value = stringInterpolator.replaceIn(input.getValue(), this, input.getUnderlyingStructure());
    return new EscapedValue(input.getUnderlyingStructure(), value);
  }

  public Expression evaluate(EmbeddedScript input) {
    String value = embeddedScriptInterpolator.replaceIn(input.getValue(), this, input.getUnderlyingStructure());
    return new EmbeddedScript(input.getUnderlyingStructure(), value);
  }

  public Expression evaluate(Variable input) {
    if (cycleDetector.wouldCycle(input)) {
      problemsHandler.variablesCycle(cycleDetector.getCycleFor(input));
      return new FaultyExpression(input);
    }
      
    FullExpressionDefinition value = scope.getValue(input);
    if (value == null || value.getNode()==null) {
      problemsHandler.undefinedVariable(input);
      return new FaultyExpression(input);
    }
    if (!(value.getNode() instanceof Expression)) {
      //FIXME !!!!!!!!!!!!!!!!! what to do here????
      return input;
    }
    Expression expression = (Expression)value.getNode();

    cycleDetector.enteringVariableValue(input);
    Expression result = evaluate(expression);
   // addToDeclarationScopes(result, );
    cycleDetector.leftVariableValue();
    return result;
  }

  public Expression evaluateIfPresent(Variable input) {
    FullExpressionDefinition value = scope.getValue(input);
    if (value == null) {
      return null;
    }

    if (!(value.getNode() instanceof Expression)) {
      //FIXME !!!!!!!!!!!!!!!!! what to do here????
      return input;
    }
  //FIXME !!!!!!!!!!!!!!!!! add to declaration scopes
    Expression expression = (Expression)value.getNode();
    return evaluate(expression);
  }

  public Expression evaluate(IndirectVariable input) {
    FullExpressionDefinition value = scope.getValue(input);
    if (!(value.getNode() instanceof Expression)) {
      //FIXME !!!!!!!!!!!!!!!!! what to do here????
      return input;
    }
    Expression expression = (Expression)value.getNode();
    if (!(expression instanceof CssString)) {
      problemsHandler.nonStringIndirection(input);
      return new FaultyExpression(input);
    }
    //FIXME !!!!!!!!!!!!!!!!! add to declaration scopes

    CssString realName = (CssString) expression;
    String realVariableName = "@" + realName.getValue();
    //FIXME: !!!!!!!!!!!! bypassing cycle detector!!!!!! (test also less.js)
    value = scope.getValue(realVariableName);
    if (value == null) {
      problemsHandler.undefinedVariable(realVariableName, realName);
      return new FaultyExpression(realName.getUnderlyingStructure());
    }
    if (!(value.getNode() instanceof Expression)) {
      //FIXME !!!!!!!!!!!!!!!!! what to do here????
      return input;
    }
    //FIXME !!!!!!!!!!!!!!!!! reusing variable ugly
    expression = (Expression)value.getNode();
    return evaluate(expression);
  }

  public Expression evaluate(Expression input) {
    //FIXME !!!!!!!!!!!!! does not belong here, hotfix for prototype experiment
    if (input instanceof FullExpressionDefinition) {
      FullExpressionDefinition ex = (FullExpressionDefinition) input;
      input = ex.getNode();
    }
    switch (input.getType()) {
    case FUNCTION:
      return evaluate((FunctionExpression) input);

    case BINARY_EXPRESSION:
      return evaluate((BinaryExpression) input);

    case LIST_EXPRESSION:
      return evaluate((ListExpression) input);

    case INDIRECT_VARIABLE:
      return evaluate((IndirectVariable) input);

    case VARIABLE:
      return evaluate((Variable) input);

    case PARENTHESES_EXPRESSION:
      return evaluate(((ParenthesesExpression) input).getEnclosedExpression());

    case SIGNED_EXPRESSION:
      return evaluate((SignedExpression) input);

    case NAMED_EXPRESSION:
      return evaluate((NamedExpression) input);
      
    case STRING_EXPRESSION:
      return evaluate((CssString) input);

    case ESCAPED_VALUE:
      return evaluate((EscapedValue) input);

    case EMBEDDED_SCRIPT:
      return evaluate((EmbeddedScript) input);

      //FIXME: !!!!!!!!!!! now, this is probably wrong, just hotefixing prototype
    case DETACHED_RULESET:
      return new FullExpressionDefinition(input, scope);

    //the value is already there, nothing to evaluate -- TODO - probably bug, should create clone()
    case IDENTIFIER_EXPRESSION:
    case COLOR_EXPRESSION:
    case NUMBER:
    case FAULTY_EXPRESSION:
    case UNICODE_RANGE_EXPRESSION:
    case EMPTY_EXPRESSION:
      return input;

    default:
      throw new BugHappened("Unknown expression type "+input.getType(), input);
    }
  }

  private boolean booleanEvalueate(Expression input) {
    if (input.getType() == ASTCssNodeType.COMPARISON_EXPRESSION)
      return booleanEvalueate((ComparisonExpression) input);

    ASTCssNode value = evaluate(input);
    if (value.getType() != ASTCssNodeType.IDENTIFIER_EXPRESSION)
      return false;

    //this comparison must be case sensitive! 
    IdentifierExpression identifier = (IdentifierExpression) value;
    return "true".equals(identifier.getValue());
  }

  public boolean booleanEvalueate(ComparisonExpression input) {
    Expression leftE = evaluate(input.getLeft());
    Expression rightE = evaluate(input.getRight());

    ComparisonExpressionOperator operator = input.getOperator();
    if (operator.getOperator() == ComparisonExpressionOperator.Operator.OPEQ)
      return comparator.equal(leftE, rightE);

    if (leftE.getType() != ASTCssNodeType.NUMBER) {
      problemsHandler.incompatibleComparisonOperand(leftE, operator);
      return false;
    }

    if (rightE.getType() != ASTCssNodeType.NUMBER) {
      problemsHandler.incompatibleComparisonOperand(rightE, operator);
      return false;
    }

    return compareNumbers((NumberExpression) leftE, (NumberExpression) rightE, operator);
  }

  private boolean compareNumbers(NumberExpression leftE, NumberExpression rightE, ComparisonExpressionOperator operator) {
    if (!canCompareDimensions(leftE.getDimension(), rightE.getDimension())) 
      return false;
    
    Double left = leftE.getValueAsDouble();
    Double right = rightE.getValueAsDouble();

    switch (operator.getOperator()) {
    case GREATER:
      return left.compareTo(right) > 0;

    case GREATER_OR_EQUAL:
      return left.compareTo(right) >= 0;

    case LOWER_OR_EQUAL:
      return left.compareTo(right) <= 0;

    case LOWER:
      return left.compareTo(right) < 0;

    default:
      throw new BugHappened("Unexpected comparison operator", operator);
    }
  }

  private boolean canCompareDimensions(Dimension left, Dimension right) {
    return true;
  }

  public Expression evaluate(FunctionExpression input) {
    Expression evaluatedParameter = evaluate(input.getParameter());
    List<Expression> splitParameters = (evaluatedParameter.getType()==ASTCssNodeType.EMPTY_EXPRESSION)?new ArrayList<Expression>() : evaluatedParameter.splitByComma();
    
    if (!input.isCssOnlyFunction()) { 
      for (FunctionsPackage pack : functions) {
        if (pack.canEvaluate(input, splitParameters))
          return pack.evaluate(input, splitParameters, evaluatedParameter);
      }
    }
    
    UnknownFunction unknownFunction = new UnknownFunction();
    return unknownFunction.evaluate(splitParameters, problemsHandler, input, evaluatedParameter);
  }

  public Expression evaluate(ListExpression input) {
    List<Expression> evaluated = new ArrayList<Expression>();
    for (Expression expression : input.getExpressions()) {
      evaluated.add(evaluate(expression));
    }
    return new ListExpression(input.getUnderlyingStructure(), evaluated, input.getOperator().clone());
  }

  public Expression evaluate(NamedExpression input) { 
    return new NamedExpression(input.getUnderlyingStructure(), input.getName(), evaluate(input.getExpression()));
  }

  public Expression evaluate(SignedExpression input) {
    Expression evaluate = evaluate(input.getExpression());
    if (evaluate instanceof NumberExpression) {
      NumberExpression negation = ((NumberExpression) evaluate).clone();
      if (input.getSign() == Sign.PLUS)
        return negation;

      negation.negate();
      negation.setOriginalString(null);
      negation.setExpliciteSign(false);
      return negation;
    }

    problemsHandler.nonNumberNegation(input);
    return new FaultyExpression(input);
  }

  public Expression evaluate(BinaryExpression input) {
    Expression leftValue = evaluate(input.getLeft());
    Expression rightValue = evaluate(input.getRight());
    if (leftValue.isFaulty() || rightValue.isFaulty())
      return new FaultyExpression(input);

    if (arithmeticCalculator.accepts(input.getOperator(), leftValue, rightValue))
      return arithmeticCalculator.evalute(input, leftValue, rightValue);

    if (colorsCalculator.accepts(input.getOperator(), leftValue, rightValue))
      return colorsCalculator.evalute(input, leftValue, rightValue);

    problemsHandler.cannotEvaluate(input);
    return new FaultyExpression(input);
  }

  public boolean guardsSatisfied(ReusableStructure mixin) {
    return evaluate(mixin.getGuards());
  }

  public boolean guardsSatisfied(RuleSet ruleSet) {
    return evaluate(ruleSet.getGuards());
  }

  public boolean evaluate(List<Guard> guards) {
    if (guards == null || guards.isEmpty())
      return true;

    //list of guards does or
    for (Guard guard : guards) {
      if (evaluate(guard))
        return true;
    }

    return false;
  }

  public boolean evaluate(Guard guard) {
    List<GuardCondition> conditions = guard.getConditions();
    if (conditions == null || conditions.isEmpty())
      return true;

    //guards can only do and
    for (GuardCondition condition : conditions) {
      if (!evaluate(condition))
        return false;
    }

    return true;
  }

  private boolean evaluate(GuardCondition guardCondition) {
    Expression condition = guardCondition.getCondition();
    boolean conditionStatus = booleanEvalueate(condition);

    return guardCondition.isNegated() ? !conditionStatus : conditionStatus;
  }

  public boolean isRatioExpression(Expression expression) {
    if (!(expression instanceof BinaryExpression))
      return false;

    BinaryExpression composed = (BinaryExpression) expression;
    if (composed.getOperator().getOperator() != Operator.SOLIDUS) {
      return false;
    }

    if (composed.getLeft().getType() != ASTCssNodeType.NUMBER)
      return false;

    if (composed.getRight().getType() != ASTCssNodeType.NUMBER)
      return false;

    return true;
  }

}

class NullScope extends BasicScope {

  private static final String NULL = "#null#";

  protected NullScope() {
    super(new LocalScope(null, Arrays.asList(NULL), NULL), new ScopesTree()); 
  }

  @Override
  public IScope getParent() {
    return this;
  }

  @Override
  public List<IScope> getChilds() {
    return Collections.emptyList();
  }

  @Override
  public List<String> getNames() {
    return Collections.emptyList();
  }

  @Override
  public boolean hasParent() {
    return false;
  }

  @Override
  public void registerVariable(AbstractVariableDeclaration node, FullExpressionDefinition replacementValue) {
  }

  @Override
  public void registerVariableIfNotPresent(String name, FullExpressionDefinition replacementValue) {
  }

  @Override
  public FullExpressionDefinition getValue(Variable variable) {
    return null;
  }

  @Override
  public FullExpressionDefinition getValue(String name) {
    return null;
  }

  @Override
  public void registerMixin(ReusableStructure mixin, IScope mixinsBodyScope) {
  }

  @Override
  public void removedFromAst() {
  }

  @Override
  public boolean isPresentInAst() {
    return false;
  }

  @Override
  public IScope getRootScope() {
    return this;
  }

  @Override
  public IScope getChildOwnerOf(ASTCssNode body) {
    return null;
  }

  @Override
  public void addNames(List<String> names) {
  }

  @Override
  public String toFullName() {
    return toLongString();
  }

  @Override
  public void registerVariable(String name, FullExpressionDefinition replacementValue) {
  }

  @Override
  public void addFilteredContent(LocalScopeFilter filter, ILocalScope source) {
  }

  @Override
  public void addAllMixins(List<FullMixinDefinition> mixins) {
  }

  @Override
  public void add(IScope otherSope) {
  }

  @Override
  public DataPlaceholder createDataPlaceholder() {
    return null;
  }

  @Override
  public void addToDataPlaceholder(IScope otherScope) {
  }

  @Override
  public void closeDataPlaceholder() {
 }

  @Override
  public List<FullMixinDefinition> getAllMixins() {
    return Collections.emptyList();
  }

  @Override
  public List<FullMixinDefinition> getMixinsByName(List<String> nameChain, ReusableStructureName name) {
    return Collections.emptyList();
  }

  @Override
  public List<FullMixinDefinition> getMixinsByName(ReusableStructureName name) {
    return Collections.emptyList();
  }

  @Override
  public List<FullMixinDefinition> getMixinsByName(String name) {
    return Collections.emptyList();
  }

  @Override
  public IScope firstChild() {
    return null;
  }

  @Override
  public boolean isBodyOwnerScope() {
    return false;
  }

  @Override
  public IScope skipBodyOwner() {
    return this;
  }

  @Override
  public String toLongString() {
    return NULL;
  }

  @Override
  public boolean seesLocalDataOf(IScope otherScope) {
    return false;
  }

  @Override
  public IScope childByOwners(ASTCssNode headNode, ASTCssNode... restNodes) {
    return this;
  }

  @Override
  public int getTreeSize() {
    return 1;
  }

}
