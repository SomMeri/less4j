package com.github.sommeri.less4j.core.compiler.expressions;

import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class UnknownFunctions implements FunctionsPackage {

  @SuppressWarnings("unused")
  private final ProblemsHandler problemsHandler;

  public UnknownFunctions(ProblemsHandler problemsHandler) {
    this.problemsHandler = problemsHandler;
  }

  @Override
  public boolean canEvaluate(FunctionExpression input, Expression parameters) {
    return true;
  }
  
  @Override
  public Expression evaluate(FunctionExpression input, Expression parameters) {
    if (!canEvaluate(input, parameters))
      return input;

    Expression oldParameter = input.getParameter();
    oldParameter.setParent(null);
    input.setParameter(parameters);
    input.configureParentToAllChilds();
    
    return input;
  }

}