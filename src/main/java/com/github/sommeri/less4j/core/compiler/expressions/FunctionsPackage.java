package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.List;

import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;

public interface FunctionsPackage {

  public boolean canEvaluate(FunctionExpression input, List<Expression> parameters);

  public Expression evaluate(FunctionExpression input, List<Expression> parameters, Expression evaluatedParameter);

  public void setProblemsHandler(FunctionProblems problemsHandler);

  public interface FunctionProblems {
    
  }
}