package com.github.sommeri.less4j.core.compiler.expressions;

import com.github.sommeri.less4j.core.ast.Expression;

public interface ExpressionFilter {
  
  Expression apply(Expression input);

  boolean accepts(String name, Expression value);
  
}
