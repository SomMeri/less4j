package com.github.sommeri.less4j.core.compiler.expressions;

import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;

public interface FunctionsPackage {

  public abstract boolean canEvaluate(FunctionExpression input, Expression parameters);

  public abstract Expression evaluate(FunctionExpression input, Expression parameters);

}