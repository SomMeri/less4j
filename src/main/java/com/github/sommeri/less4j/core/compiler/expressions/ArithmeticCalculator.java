package com.github.sommeri.less4j.core.compiler.expressions;

import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.BinaryExpression;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.BinaryExpressionOperator;
import com.github.sommeri.less4j.core.ast.FaultyExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression.Dimension;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.problems.BugHappened;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

class ArithmeticCalculator {
  
  private ProblemsHandler problemsHandler;

  public ArithmeticCalculator(ProblemsHandler problemsHandler) {
    this.problemsHandler = problemsHandler;
  }

  public Expression evalute(BinaryExpression originalExpression, Expression firstNumber, Expression secondNumber) {
    NumberExpression first = (NumberExpression) firstNumber;
    NumberExpression second = (NumberExpression) secondNumber;

    BinaryExpressionOperator operator = originalExpression.getOperator();
    switch (operator.getOperator()) {
    case SOLIDUS:
      return divide(first, second, originalExpression);
    case STAR:
      return multiply(first, second, originalExpression);
    case MINUS:
      return subtract(first, second, originalExpression);
    case PLUS:
      return add(first, second, originalExpression);

    default:
      throw new BugHappened("Unknown operator.", operator);
    }

  }

  private Expression subtract(NumberExpression first, NumberExpression second, BinaryExpression originalExpression) {
    return subtractNumbers(first, second, originalExpression.getUnderlyingStructure());
  }

  private Expression subtractNumbers(NumberExpression first, NumberExpression second, HiddenTokenAwareTree parentToken) {
    Double firstVal = first.getValueAsDouble();
    Double secondVal = second.getValueAsDouble();
    Double resultVal = firstVal - secondVal;

    return createResultNumber(parentToken, resultVal, first, second);
  }

  private Expression multiply(NumberExpression first, NumberExpression second, BinaryExpression originalExpression) {
    return multiplyNumbers(first, second, originalExpression.getUnderlyingStructure());
  }

  private Expression multiplyNumbers(NumberExpression first, NumberExpression second, HiddenTokenAwareTree parentToken) {
    Double firstVal = first.getValueAsDouble();
    Double secondVal = second.getValueAsDouble();
    Double resultVal = firstVal * secondVal;

    return createResultNumber(parentToken, resultVal, first, second);
  }

  private Expression divide(NumberExpression first, NumberExpression second, BinaryExpression originalExpression) {
    return divideNumbers(first, second, originalExpression.getUnderlyingStructure());
  }

  private Expression divideNumbers(NumberExpression first, NumberExpression second, HiddenTokenAwareTree parentToken) {
    Double firstVal = first.getValueAsDouble();
    Double secondVal = second.getValueAsDouble();
    Double resultVal = firstVal / secondVal;
    
    return createResultNumber(parentToken, resultVal, first, second);
  }

  private Expression add(NumberExpression first, NumberExpression second, BinaryExpression originalExpression) {
    return addNumbers(first, second, originalExpression.getUnderlyingStructure());
  }

  private Expression addNumbers(NumberExpression first, NumberExpression second, HiddenTokenAwareTree parentToken) {
    Double firstVal = first.getValueAsDouble();
    Double secondVal = second.getValueAsDouble();
    Double resultVal = firstVal + secondVal;

    return createResultNumber(parentToken, resultVal, first, second);
  }

  private Expression createResultNumber(HiddenTokenAwareTree parentToken, Double resultVal, NumberExpression first, NumberExpression second) {
    if (resultVal.isInfinite()) {
      problemsHandler.divisionByZero(second);
      return new FaultyExpression(second);
    }
    
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

  public boolean accepts(BinaryExpressionOperator operator, Expression first, Expression second) {
    return acceptedOperand(first, second);
  }

  private boolean acceptedOperand(Expression first, Expression second) {
    return first.getType() == ASTCssNodeType.NUMBER && second.getType() == ASTCssNodeType.NUMBER;
  }

}
