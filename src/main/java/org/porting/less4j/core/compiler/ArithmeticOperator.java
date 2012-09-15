package org.porting.less4j.core.compiler;

import org.porting.less4j.core.ast.ASTCssNodeType;
import org.porting.less4j.core.ast.ComposedExpression;
import org.porting.less4j.core.ast.Expression;
import org.porting.less4j.core.ast.ExpressionOperator;
import org.porting.less4j.core.ast.NumberExpression;
import org.porting.less4j.core.ast.NumberExpression.Dimension;
import org.porting.less4j.core.parser.HiddenTokenAwareTree;

//TODO document we support only operations on numbers for now - as is less.js
public class ArithmeticOperator {

  public Expression evalute(ComposedExpression originalExpression, Expression firstNumber, Expression secondNumber) {
    validateParameters(firstNumber, secondNumber);
    NumberExpression first = (NumberExpression) firstNumber;
    NumberExpression second = (NumberExpression) secondNumber;

    ExpressionOperator operator = originalExpression.getOperator();
    switch (operator.getOperator()) {
    case SOLIDUS:
      return divide(first, second, originalExpression);
    case STAR:
      return multiply(first, second, originalExpression);
    case MINUS:
      return subtract(first, second, originalExpression);
    case PLUS:
      return add(first, second, originalExpression);

    case COMMA:
    case EMPTY_OPERATOR:
      throw new CompileException("Not an arithmetic operator.", operator);

    default:
      throw new CompileException("Unknown operator.", operator);
    }

  }

  private Expression subtract(NumberExpression first, NumberExpression second, ComposedExpression originalExpression) {
    return subtractNumbers(first, second, originalExpression.getUnderlyingStructure());
  }

  private Expression subtractNumbers(NumberExpression first, NumberExpression second, HiddenTokenAwareTree parentToken) {
    Double firstVal = first.getValueAsDouble();
    Double secondVal = second.getValueAsDouble();
    Double resultVal = firstVal - secondVal;

    //FIXME: document: inherits dimension from the first member with the dimension
    return createResultNumber(parentToken, resultVal, first, second);
  }

  private Expression multiply(NumberExpression first, NumberExpression second, ComposedExpression originalExpression) {
    return multiplyNumbers(first, second, originalExpression.getUnderlyingStructure());
  }

  private Expression multiplyNumbers(NumberExpression first, NumberExpression second, HiddenTokenAwareTree parentToken) {
    Double firstVal = first.getValueAsDouble();
    Double secondVal = second.getValueAsDouble();
    Double resultVal = firstVal * secondVal;

    return createResultNumber(parentToken, resultVal, first, second);
  }

  private Expression divide(NumberExpression first, NumberExpression second, ComposedExpression originalExpression) {
    return divideNumbers(first, second, originalExpression.getUnderlyingStructure());
  }

  private Expression divideNumbers(NumberExpression first, NumberExpression second, HiddenTokenAwareTree parentToken) {
    Double firstVal = first.getValueAsDouble();
    Double secondVal = second.getValueAsDouble();
    Double resultVal = firstVal / secondVal;

    return createResultNumber(parentToken, resultVal, first, second);
  }

  private Expression add(NumberExpression first, NumberExpression second, ComposedExpression originalExpression) {
    return addNumbers(first, second, originalExpression.getUnderlyingStructure());
  }

  private Expression addNumbers(NumberExpression first, NumberExpression second, HiddenTokenAwareTree parentToken) {
    Double firstVal = first.getValueAsDouble();
    Double secondVal = second.getValueAsDouble();
    Double resultVal = firstVal + secondVal;

    return createResultNumber(parentToken, resultVal, first, second);
  }

  public Expression createResultNumber(HiddenTokenAwareTree parentToken, Double resultVal, NumberExpression first, NumberExpression second) {
    Dimension dimension = null;
    String suffix = null;
    if (first.getDimension()!=Dimension.NUMBER) {
      dimension=first.getDimension();
      suffix = first.getSuffix();
    } else {
      dimension=second.getDimension();
      suffix = second.getSuffix();
    }
    return new NumberExpression(parentToken, resultVal, suffix, null, dimension);
  }

  public boolean accepts(ExpressionOperator operator) {
    return operator.getOperator() != ExpressionOperator.Operator.COMMA && operator.getOperator() != ExpressionOperator.Operator.EMPTY_OPERATOR;
  }

  private void validateParameters(Expression first, Expression second) {
    if (!(first.getType() == ASTCssNodeType.NUMBER))
      throw new CompileException("Only operations on numbers are supported.", first);
    if (second.getType() != ASTCssNodeType.NUMBER)
      throw new CompileException("Only operations on numbers are supported.", second);
  }

}
