package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.List;

import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class UnknownFunction implements Function {

  @Override
  public Expression evaluate(List<Expression> parameters, ProblemsHandler problemsHandler, FunctionExpression call, Expression evaluatedParameter) {
    Expression oldParameter = call.getParameter();
    oldParameter.setParent(null);
    call.setParameter(evaluatedParameter);
    call.configureParentToAllChilds();
    
    return call;
  }

  @Override
  public boolean acceptsParameters(List<Expression> parameters) {
    return true;
  }

}
