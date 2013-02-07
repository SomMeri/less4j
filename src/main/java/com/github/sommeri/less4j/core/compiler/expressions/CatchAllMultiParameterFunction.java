package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.List;

import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FaultyExpression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

/**
 * The function will accept any types of parameters. If they are not compatible with what
 * functions do, it will throw an error.
 *
 */
abstract class CatchAllMultiParameterFunction extends AbstractMultiParameterFunction {

  @Override
  public Expression evaluate(List<Expression> parameters, ProblemsHandler problemsHandler, FunctionExpression call, Expression evaluatedParameter) {
    if (parameters.size() < getMinParameters()) {
      problemsHandler.wrongNumberOfArgumentsToFunction(call, getName(), getMinParameters());
      return new FaultyExpression(call);
    }

    if (parameters.size() > getMaxParameters()) {
      problemsHandler.wrongNumberOfArgumentsToFunction(call, getName(), getMaxParameters());
      return new FaultyExpression(call);
    }

    for (int i = 0; i < parameters.size(); i++) {
      if (!validateParameter(parameters.get(i), i, problemsHandler)) {
        return new FaultyExpression(call);
      }
    }

    return evaluate(parameters, problemsHandler, call.getUnderlyingStructure());
  }

  protected abstract Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, HiddenTokenAwareTree token);

}