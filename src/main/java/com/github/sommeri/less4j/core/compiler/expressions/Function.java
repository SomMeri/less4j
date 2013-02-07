package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.List;

import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

interface Function {
  
  Expression evaluate(List<Expression> parameters, ProblemsHandler problemsHandler, FunctionExpression call, Expression evaluatedParameter);

  boolean acceptsParameters(List<Expression> parameters);
  
}