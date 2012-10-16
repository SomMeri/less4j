package com.github.sommeri.less4j.core.compiler.expressions;

import com.github.sommeri.less4j.core.ast.ComposedExpression;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.ExpressionOperator;

class ListCalculator {

  public boolean accepts(ExpressionOperator operator) {
    return operator.getOperator()== ExpressionOperator.Operator.COMMA || operator.getOperator() == ExpressionOperator.Operator.EMPTY_OPERATOR;
  }

  public Expression evalute(ComposedExpression originalExpression, Expression leftValue, Expression rightValue) {
    return new ComposedExpression(originalExpression.getUnderlyingStructure(), leftValue, originalExpression.getOperator(), rightValue);
  }

}
