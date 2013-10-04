package com.github.sommeri.less4j.core.compiler.expressions;

import com.github.sommeri.less4j.core.ast.NumberExpression;

/**
 * 
 * Requires perfect match e.g. -0!=0.
 *
 */
public class FormalisticExpressionComparator extends ExpressionComparator {

  @Override
  protected boolean numberEqual(NumberExpression n1, NumberExpression n2) {
    return  signEq(n1, n2) && suffixEq(n1, n2) && valueEq(n1, n2);
  }

  private boolean signEq(NumberExpression n1, NumberExpression n2) {
    return n1.hasExpliciteSign() == n2.hasExpliciteSign();
  }

  private boolean valueEq(NumberExpression n1, NumberExpression n2) {
    return equals(n1.getValueAsDouble(), n2.getValueAsDouble());
  }

  private boolean suffixEq(NumberExpression n1, NumberExpression n2) {
    return equals(n1.getSuffix(), n2.getSuffix());
  }

  protected boolean equals(Double value, Double value2) {
    if (value == null || value2==null)
      return value2 == null && value==null;

    return value.compareTo(value2) == 0;
  }
}
