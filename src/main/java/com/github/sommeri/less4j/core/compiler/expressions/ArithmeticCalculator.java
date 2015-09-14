package com.github.sommeri.less4j.core.compiler.expressions;

import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.BinaryExpression;
import com.github.sommeri.less4j.core.ast.BinaryExpressionOperator;
import com.github.sommeri.less4j.core.ast.Expression;
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
    
    //convert to the same unit
    String resultSuffix = resultDimension(first, second);
    
    BinaryExpressionOperator operator = originalExpression.getOperator();
    
    if (shouldConvert(operator)) {
      first = first.convertIfPossible(resultSuffix);
      second = second.convertIfPossible(resultSuffix);
    }
    
    switch (operator.getOperator()) {
    case SOLIDUS:
      return divide(first, second, resultSuffix, originalExpression);
    case STAR:
      return multiply(first, second, resultSuffix, originalExpression);
    case MINUS:
      return subtract(first, second, resultSuffix, originalExpression);
    case PLUS:
      return add(first, second, resultSuffix, originalExpression);

    default:
      throw new BugHappened("Unknown operator.", operator);
    }

  }

  private boolean shouldConvert(BinaryExpressionOperator operator) {
    return operator.getOperator()==BinaryExpressionOperator.Operator.PLUS || operator.getOperator()==BinaryExpressionOperator.Operator.MINUS;
  }

  private Expression subtract(NumberExpression first, NumberExpression second, String resultSuffix, BinaryExpression originalExpression) {
    return subtractNumbers(first, second, resultSuffix, originalExpression.getUnderlyingStructure());
  }

  private Expression subtractNumbers(NumberExpression first, NumberExpression second, String resultSuffix, HiddenTokenAwareTree parentToken) {
    Double firstVal = first.getValueAsDouble();
    Double secondVal = second.getValueAsDouble();
    Double resultVal = firstVal - secondVal;

    return createResultNumber(parentToken, resultVal, first, second, resultSuffix);
  }

  private Expression multiply(NumberExpression first, NumberExpression second, String resultSuffix, BinaryExpression originalExpression) {
    return multiplyNumbers(first, second, resultSuffix, originalExpression.getUnderlyingStructure());
  }

  private Expression multiplyNumbers(NumberExpression first, NumberExpression second, String resultSuffix, HiddenTokenAwareTree parentToken) {
    Double firstVal = first.getValueAsDouble();
    Double secondVal = second.getValueAsDouble();
    Double resultVal = firstVal * secondVal;

    return createResultNumber(parentToken, resultVal, first, second, resultSuffix);
  }

  private Expression divide(NumberExpression first, NumberExpression second, String resultSuffix, BinaryExpression originalExpression) {
    return divideNumbers(first, second, resultSuffix, originalExpression.getUnderlyingStructure());
  }

  private Expression divideNumbers(NumberExpression first, NumberExpression second, String resultSuffix, HiddenTokenAwareTree parentToken) {
    Double firstVal = first.getValueAsDouble();
    Double secondVal = second.getValueAsDouble();
    Double resultVal = firstVal / secondVal;
    
    return createResultNumber(parentToken, resultVal, first, second, resultSuffix);
  }

  private Expression add(NumberExpression first, NumberExpression second, String resultSuffix, BinaryExpression originalExpression) {
    return addNumbers(first, second, resultSuffix, originalExpression.getUnderlyingStructure());
  }

  private Expression addNumbers(NumberExpression first, NumberExpression second, String resultSuffix, HiddenTokenAwareTree parentToken) {
    Double firstVal = first.getValueAsDouble();
    Double secondVal = second.getValueAsDouble();
    Double resultVal = firstVal + secondVal;

    return createResultNumber(parentToken, resultVal, first, second, resultSuffix);
  }

  private Expression createResultNumber(HiddenTokenAwareTree parentToken, Double resultVal, NumberExpression first, NumberExpression second, String resultSuffix) {
    if (resultVal.isInfinite()) {
      problemsHandler.divisionByZero(second);
      return new FaultyExpression(second);
    }
    
    return new NumberExpression(parentToken, resultVal, resultSuffix, null, Dimension.forSuffix(resultSuffix));
  }

  private String resultDimension(NumberExpression first, NumberExpression second) {
    if (first.getDimension()!=Dimension.NUMBER) {
      return first.getSuffix();
    } 

    return second.getSuffix();
  }

  public boolean accepts(BinaryExpressionOperator operator, Expression first, Expression second) {
    return acceptedOperand(first, second);
  }

  private boolean acceptedOperand(Expression first, Expression second) {
    return first.getType() == ASTCssNodeType.NUMBER && second.getType() == ASTCssNodeType.NUMBER;
  }

}
