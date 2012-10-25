package com.github.sommeri.less4j.core.compiler.expressions;

import com.github.sommeri.less4j.core.ast.NumberExpression;

public class PatternsComparator extends ExpressionComparator {

  @Override
  protected boolean numberEqual(NumberExpression pattern, NumberExpression numberExpression) {
    return equals(pattern.getSuffix(), numberExpression.getSuffix()) && equals(pattern.getValueAsDouble(), numberExpression.getValueAsDouble());
  }


}
