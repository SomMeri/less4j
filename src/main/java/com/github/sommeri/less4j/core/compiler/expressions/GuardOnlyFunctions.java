package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class GuardOnlyFunctions extends BuiltInFunctionsPack {

  protected static final String DEFAULT = "default";

  private Map<String, Function> functions = new HashMap<String, Function>();

  public GuardOnlyFunctions(ProblemsHandler problemsHandler, boolean assumeDefault) {
    super(problemsHandler);
    functions.put(DEFAULT, new Default(assumeDefault));
  }

  @Override
  protected Map<String, Function> getFunctions() {
    return functions;
  }

}

class Default extends AbstractFunction {

  private final String value;

  public Default(boolean value) {
    this.value = value ? "true" : "false";
  }

  @Override
  public Expression evaluate(List<Expression> parameters, ProblemsHandler problemsHandler, FunctionExpression call, Expression evaluatedParameter) {
    return new IdentifierExpression(call.getUnderlyingStructure(), value);
  }

}
