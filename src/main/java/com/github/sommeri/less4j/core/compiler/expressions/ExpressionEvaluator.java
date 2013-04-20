package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.AbstractVariableDeclaration;
import com.github.sommeri.less4j.core.ast.ComparisonExpression;
import com.github.sommeri.less4j.core.ast.ComparisonExpressionOperator;
import com.github.sommeri.less4j.core.ast.ComposedExpression;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.EscapedValue;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.ExpressionOperator;
import com.github.sommeri.less4j.core.ast.ExpressionOperator.Operator;
import com.github.sommeri.less4j.core.ast.FaultyExpression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.Guard;
import com.github.sommeri.less4j.core.ast.GuardCondition;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.ast.IndirectVariable;
import com.github.sommeri.less4j.core.ast.NamedExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression.Dimension;
import com.github.sommeri.less4j.core.ast.ParenthesesExpression;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.ReusableStructureName;
import com.github.sommeri.less4j.core.ast.SignedExpression;
import com.github.sommeri.less4j.core.ast.SignedExpression.Sign;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.compiler.expressions.strings.StringInterpolator;
import com.github.sommeri.less4j.core.compiler.scopes.FullMixinDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.Scope;
import com.github.sommeri.less4j.core.problems.BugHappened;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class ExpressionEvaluator {

  private VariableCycleDetector cycleDetector = new VariableCycleDetector();
  private final Scope scope;
  private final ProblemsHandler problemsHandler;
  private ArithmeticCalculator arithmeticCalculator;
  private ListCalculator listCalculator = new ListCalculator();
  private ColorsCalculator colorsCalculator;
  private ExpressionComparator comparator = new GuardsComparator();
  private List<FunctionsPackage> functions = new ArrayList<FunctionsPackage>();
  private StringInterpolator stringInterpolator = new StringInterpolator();

  public ExpressionEvaluator(ProblemsHandler problemsHandler) {
    this(new NullScope(), problemsHandler);
  }

  public ExpressionEvaluator(Scope scope, ProblemsHandler problemsHandler) {
    super();
    this.scope = scope == null ? new NullScope() : scope;
    this.problemsHandler = problemsHandler;
    arithmeticCalculator = new ArithmeticCalculator(problemsHandler);
    colorsCalculator = new ColorsCalculator(problemsHandler);
    functions.add(new MathFunctions(problemsHandler));
    functions.add(new StringFunctions(problemsHandler));
    functions.add(new ColorFunctions(problemsHandler));
    functions.add(new MiscFunctions(problemsHandler));
    functions.add(new TypeFunctions(problemsHandler));
  }

  public Expression joinAll(List<Expression> allArguments, ASTCssNode parent) {
    if (allArguments.isEmpty())
      return new IdentifierExpression(parent.getUnderlyingStructure(), "");

    Iterator<Expression> iterator = allArguments.iterator();
    Expression result = iterator.next();
    while (iterator.hasNext()) {
      result = new ComposedExpression(parent.getUnderlyingStructure(), result, new ExpressionOperator(parent.getUnderlyingStructure()), iterator.next());
    }

    return result;
  }

  public List<Expression> evaluateAll(List<Expression> expressions) {
    List<Expression> values = new ArrayList<Expression>();
    for (Expression argument : expressions) {
      values.add(evaluate(argument));
    }

    return values;
  }

  public Scope evaluateValues(Scope scope) {
    Scope result = Scope.createDummyScope();
    result.fillByFilteredVariables(toEvaluationFilter(), scope);
    return result;
  }

  private ExpressionFilter toEvaluationFilter() {
    return new ExpressionFilter() {
      
      @Override
      public Expression apply(Expression input) {
        return evaluate(input);
      }
    };
  }

  public Expression evaluate(CssString input) {
    String value = stringInterpolator.replaceIn(input.getValue(), this, input.getUnderlyingStructure());
    return new CssString(input.getUnderlyingStructure(), value, input.getQuoteType());
  }
  
  public Expression evaluate(EscapedValue input) {
    String value = stringInterpolator.replaceIn(input.getValue(), this, input.getUnderlyingStructure());
    return new EscapedValue(input.getUnderlyingStructure(), value);
  }

  public Expression evaluate(Variable input) {
    if (cycleDetector.wouldCycle(input)) {
      problemsHandler.variablesCycle(cycleDetector.getCycleFor(input));
      return new FaultyExpression(input);
    }
      
    Expression value = scope.getValue(input);
    if (value == null) {
      problemsHandler.undefinedVariable(input);
      return new FaultyExpression(input);
    }

    cycleDetector.enteringVariableValue(input);
    Expression result = evaluate(value);
    cycleDetector.leftVariableValue();
    return result;
  }

  public Expression evaluateIfPresent(Variable input) {
    Expression value = scope.getValue(input);
    if (value == null) {
      return null;
    }

    return evaluate(value);
  }

  public Expression evaluate(IndirectVariable input) {
    Expression value = scope.getValue(input);
    if (!(value instanceof CssString)) {
      problemsHandler.nonStringIndirection(input);
      return new FaultyExpression(input);
    }

    CssString realName = (CssString) value;
    String realVariableName = "@" + realName.getValue();
    value = scope.getValue(realVariableName);
    if (value == null) {
      problemsHandler.undefinedVariable(realVariableName, realName);
      return new FaultyExpression(realName.getUnderlyingStructure());
    }
    return evaluate(value);
  }

  public Expression evaluate(Expression input) {
    switch (input.getType()) {
    case FUNCTION:
      return evaluate((FunctionExpression) input);

    case COMPOSED_EXPRESSION:
      return evaluate((ComposedExpression) input);

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

    //the value is already there, nothing to evaluate
    case IDENTIFIER_EXPRESSION:
    case COLOR_EXPRESSION:
    case NUMBER:
    case FAULTY_EXPRESSION:
    case UNICODE_RANGE_EXPRESSION:
    case EMPTY_EXPRESSION:
      return input;

    default:
      throw new BugHappened("Unknown expression type", input);
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
    List<Expression> splitParameters = evaluatedParameter.splitByComma();
    
    if (!input.isCssOnlyFunction()) { 
      for (FunctionsPackage pack : functions) {
        if (pack.canEvaluate(input, splitParameters))
          return pack.evaluate(input, splitParameters, evaluatedParameter);
      }
    }
    
    UnknownFunction unknownFunction = new UnknownFunction();
    return unknownFunction.evaluate(splitParameters, problemsHandler, input, evaluatedParameter);
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

  public Expression evaluate(ComposedExpression input) {
    Expression leftValue = evaluate(input.getLeft());
    Expression rightValue = evaluate(input.getRight());
    if (leftValue.isFaulty() || rightValue.isFaulty())
      return new FaultyExpression(input);

    if (arithmeticCalculator.accepts(input.getOperator(), leftValue, rightValue))
      return arithmeticCalculator.evalute(input, leftValue, rightValue);

    if (colorsCalculator.accepts(input.getOperator(), leftValue, rightValue))
      return colorsCalculator.evalute(input, leftValue, rightValue);

    if (listCalculator.accepts(input.getOperator()))
      return listCalculator.evalute(input, leftValue, rightValue);

    problemsHandler.cannotEvaluate(input);
    return new FaultyExpression(input);
  }

  public boolean guardsSatisfied(ReusableStructure mixin) {
    return evaluate(mixin.getGuards());
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
    if (!(expression instanceof ComposedExpression))
      return false;

    ComposedExpression composed = (ComposedExpression) expression;
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

class NullScope extends Scope {

  private static final String NULL = "null";

  protected NullScope() {
    super(NULL, null, "  ");
  }

  @Override
  public Scope getParent() {
    return this;
  }

  @Override
  public List<Scope> getChilds() {
    return Collections.emptyList();
  }

  @Override
  public List<String> getNames() {
    return super.getNames();
  }

  @Override
  public boolean hasParent() {
    return false;
  }

  @Override
  public void registerVariable(AbstractVariableDeclaration declaration) {
  }

  @Override
  public void registerVariable(AbstractVariableDeclaration node, Expression replacementValue) {
  }

  @Override
  public void registerVariableIfNotPresent(String name, Expression replacementValue) {
  }

  @Override
  public Expression getValue(Variable variable) {
    return null;
  }

  @Override
  public Expression getValue(String name) {
    return null;
  }

  @Override
  public void registerMixin(ReusableStructure mixin, Scope mixinsBodyScope) {
  }

  @Override
  public List<FullMixinDefinition> getNearestMixins(ReusableStructureName name) {
    return Collections.emptyList();
  }

  @Override
  public List<Scope> findMatchingChilds(List<String> nameChain) {
    return Collections.emptyList();
  }

  @Override
  public Scope copyWithChildChain() {
    return this;
  }

  @Override
  public Scope copyWithChildChain(Scope parent) {
    return this;
  }

  @Override
  public Scope copyWithParentsChain() {
    return this;
  }

  @Override
  public void setParent(Scope parent) {
  }

  @Override
  public void removedFromTree() {
  }

  @Override
  public boolean isPresentInTree() {
    return false;
  }

  @Override
  public Scope getRootScope() {
    return this;
  }

  @Override
  public Scope getChildOwnerOf(ASTCssNode body) {
    return null;
  }

}
