package com.github.sommeri.less4j.core.compiler.expressions;

import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.ComposedExpression;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.ExpressionOperator;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression.Dimension;
import com.github.sommeri.less4j.core.compiler.CompileException;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

//FIXME: make protected
public class ArithmeticCalculator {

  public NumberExpression evalute(ComposedExpression originalExpression, Expression firstNumber, Expression secondNumber) {
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

  private NumberExpression subtract(NumberExpression first, NumberExpression second, ComposedExpression originalExpression) {
    return subtractNumbers(first, second, originalExpression.getUnderlyingStructure());
  }

  private NumberExpression subtractNumbers(NumberExpression first, NumberExpression second, HiddenTokenAwareTree parentToken) {
    Double firstVal = first.getValueAsDouble();
    Double secondVal = second.getValueAsDouble();
    Double resultVal = firstVal - secondVal;

    return createResultNumber(parentToken, resultVal, first, second);
  }

  private NumberExpression multiply(NumberExpression first, NumberExpression second, ComposedExpression originalExpression) {
    return multiplyNumbers(first, second, originalExpression.getUnderlyingStructure());
  }

  private NumberExpression multiplyNumbers(NumberExpression first, NumberExpression second, HiddenTokenAwareTree parentToken) {
    Double firstVal = first.getValueAsDouble();
    Double secondVal = second.getValueAsDouble();
    Double resultVal = firstVal * secondVal;

    return createResultNumber(parentToken, resultVal, first, second);
  }

  private NumberExpression divide(NumberExpression first, NumberExpression second, ComposedExpression originalExpression) {
    return divideNumbers(first, second, originalExpression.getUnderlyingStructure());
  }

  private NumberExpression divideNumbers(NumberExpression first, NumberExpression second, HiddenTokenAwareTree parentToken) {
    Double firstVal = first.getValueAsDouble();
    Double secondVal = second.getValueAsDouble();
    Double resultVal = firstVal / secondVal;

    return createResultNumber(parentToken, resultVal, first, second);
  }

  private NumberExpression add(NumberExpression first, NumberExpression second, ComposedExpression originalExpression) {
    return addNumbers(first, second, originalExpression.getUnderlyingStructure());
  }

  private NumberExpression addNumbers(NumberExpression first, NumberExpression second, HiddenTokenAwareTree parentToken) {
    Double firstVal = first.getValueAsDouble();
    Double secondVal = second.getValueAsDouble();
    Double resultVal = firstVal + secondVal;

    return createResultNumber(parentToken, resultVal, first, second);
  }

  private NumberExpression createResultNumber(HiddenTokenAwareTree parentToken, Double resultVal, NumberExpression first, NumberExpression second) {
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

  public boolean accepts(ExpressionOperator operator, Expression first, Expression second) {
    if (!acceptedOperand(first, second))
      return false;
    
    return acceptedOperator(operator);
  }

  private boolean acceptedOperator(ExpressionOperator operator) {
    return operator.getOperator() != ExpressionOperator.Operator.COMMA && operator.getOperator() != ExpressionOperator.Operator.EMPTY_OPERATOR;
  }

  private boolean acceptedOperand(Expression first, Expression second) {
    return first.getType() == ASTCssNodeType.NUMBER && second.getType() == ASTCssNodeType.NUMBER;
  }

}
