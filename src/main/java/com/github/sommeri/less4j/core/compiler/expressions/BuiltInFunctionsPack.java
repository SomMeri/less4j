package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.List;
import java.util.Map;

import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public abstract class BuiltInFunctionsPack implements FunctionsPackage {

  private final ProblemsHandler problemsHandler;

  public BuiltInFunctionsPack(ProblemsHandler problemsHandler) {
    this.problemsHandler = problemsHandler;
  }

  @Override
  public final boolean canEvaluate(FunctionExpression input, List<Expression> parameters) {
    return getFunctions().containsKey(normalizeName(input));
  }

  @Override
  public final Expression evaluate(FunctionExpression input, List<Expression> parameters, Expression evaluatedParameter) {
    if (!canEvaluate(input, parameters))
      return input;

    Function function = getFunctions().get(normalizeName(input));
    return function.evaluate(parameters, problemsHandler, input, evaluatedParameter);
  }

  protected abstract Map<String, Function> getFunctions();

  private String normalizeName(FunctionExpression input) {
    return input.getName().toLowerCase();
  }

}
