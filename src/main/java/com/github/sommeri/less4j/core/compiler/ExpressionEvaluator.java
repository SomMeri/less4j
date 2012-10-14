package com.github.sommeri.less4j.core.compiler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.ComparisonExpression;
import com.github.sommeri.less4j.core.ast.ComparisonExpressionOperator;
import com.github.sommeri.less4j.core.ast.ComposedExpression;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.ExpressionOperator.Operator;
import com.github.sommeri.less4j.core.ast.ExpressionOperator;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.Guard;
import com.github.sommeri.less4j.core.ast.GuardCondition;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.ast.IndirectVariable;
import com.github.sommeri.less4j.core.ast.NamedExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.ParenthesesExpression;
import com.github.sommeri.less4j.core.ast.SignedExpression;
import com.github.sommeri.less4j.core.ast.SignedExpression.Sign;
import com.github.sommeri.less4j.core.ast.Variable;

public class ExpressionEvaluator {

  private final ActiveScope variableScope;
  private ArithmeticOperator arithmeticEngine = new ArithmeticOperator();
  private ExpressionComparator comparator = new GuardsComparator();
  

  public ExpressionEvaluator(ActiveScope variableScope) {
    super();
    this.variableScope = variableScope;
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

  public Expression evaluate(Variable input) {
    Expression value = variableScope.getDeclaredValue(input);
    return evaluate(value);
  }

  public Expression evaluate(IndirectVariable input) {
    Expression value = variableScope.getDeclaredValue(input);
    CssString realName = convertToStringExpression(evaluate(value), input);
    return evaluate(variableScope.getDeclaredValue("@" + realName.getValue(), realName));
  }

  private CssString convertToStringExpression(Expression evaluate, Expression errorNode) {
    if (!(evaluate instanceof CssString))
      throw new CompileException("Variable indirection works only with string values.", errorNode);

    return (CssString) evaluate;
  }

  //FIXME: refactoring change to reusable expression type switch
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
      return ((NamedExpression) input).getExpression();

    //the value is already there, nothing to evaluate
    case IDENTIFIER_EXPRESSION:
    case COLOR_EXPRESSION:
    case NUMBER:
    case STRING_EXPRESSION:
      return input;

    default:
      throw new CompileException("Unknown expression type", input);
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

  //FIXME: add also identifiers comparison
  public boolean booleanEvalueate(ComparisonExpression input) {
    Expression leftE = evaluate(input.getLeft());
    Expression rightE = evaluate(input.getRight());

    comparator.validateSimpleExpression(leftE);
    comparator.validateSimpleExpression(rightE);

    ComparisonExpressionOperator operator = input.getOperator();
    if (operator.getOperator()==ComparisonExpressionOperator.Operator.OPEQ)
      return comparator.equal(leftE, rightE);

    if (leftE.getType() != ASTCssNodeType.NUMBER)
      throw new CompileException("The operator " +operator+ " can be used only with numbers.", leftE);

    if (rightE.getType() != ASTCssNodeType.NUMBER)
      throw new CompileException("The operator " +operator+ " can be used only with numbers.", rightE);

    return compareNumbers((NumberExpression)leftE, (NumberExpression)rightE, operator);
  }

  private boolean compareNumbers(NumberExpression leftE, NumberExpression rightE, ComparisonExpressionOperator operator) {
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
      throw new CompileException("Unexpected comparison operator", operator);
    }
  }

  public Expression evaluate(FunctionExpression input) {
    return input;
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

    throw new CompileException("The expression does not evaluate to number and can not be negated.", input);
  }

  public Expression evaluate(ComposedExpression input) {
    Expression leftValue = evaluate(input.getLeft());
    Expression rightValue = evaluate(input.getRight());

    if (arithmeticEngine.accepts(input.getOperator()))
      return arithmeticEngine.evalute(input, leftValue, rightValue);

    return new ComposedExpression(input.getUnderlyingStructure(), leftValue, input.getOperator(), rightValue);
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
