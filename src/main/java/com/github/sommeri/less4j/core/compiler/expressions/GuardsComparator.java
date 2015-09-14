package com.github.sommeri.less4j.core.compiler.expressions;

import com.github.sommeri.less4j.core.ast.EscapedValue;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression;

public class GuardsComparator extends ExpressionComparator {
  
  @Override
  protected boolean numberEqual(NumberExpression pattern, NumberExpression numberExpression) {
    if (!pattern.convertibleTo(numberExpression))
      return false;

    return equals(pattern.getValueAsDouble(), numberExpression.getValueAsDouble());
  }

  @Override
  protected boolean equalIdentifier(IdentifierExpression pattern, Expression expression) {
    if (expression instanceof EscapedValue) {
      return equals(pattern.getValue(), ((EscapedValue) expression).getValue());
    }

    return super.equalIdentifier(pattern, expression);
  }

  @Override
  protected boolean equalEscapedValue(EscapedValue pattern, Expression expression) {
    if (expression instanceof IdentifierExpression) {
      return equals(pattern.getValue(), ((IdentifierExpression) expression).getValue());
    }

    return super.equalEscapedValue(pattern, expression);
  }

}
