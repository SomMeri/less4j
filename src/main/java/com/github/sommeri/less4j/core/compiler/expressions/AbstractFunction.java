package com.github.sommeri.less4j.core.compiler.expressions;

import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression.Dimension;

abstract class AbstractFunction implements Function {

  static double scaled(NumberExpression n, int size) {
    if (n.getDimension() == Dimension.PERCENTAGE) {
      return n.getValueAsDouble() * size / 100;
    } else {
      return number(n);
    }
  }

  static double number(NumberExpression n) {
    if (n.getDimension() == Dimension.PERCENTAGE) {
      return n.getValueAsDouble() / 100;
    } else {
      return n.getValueAsDouble();
    }
  }

}
