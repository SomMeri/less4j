package com.github.sommeri.less4j.core.compiler;

import com.github.sommeri.less4j.core.ast.NumberExpression;

public class GuardsComparator extends ExpressionComparator {

  @Override
  protected boolean numberEqual(NumberExpression pattern, NumberExpression numberExpression) {
    return equals(pattern.getValueAsDouble(), numberExpression.getValueAsDouble());
  }


}
