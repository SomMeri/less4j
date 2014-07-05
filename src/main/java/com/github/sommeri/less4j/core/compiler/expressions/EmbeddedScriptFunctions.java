package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.EmbeddedScript;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FaultyExpression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class EmbeddedScriptFunctions extends BuiltInFunctionsPack {

  protected static final String EMBEDDED_SCRIPT = "`";
  protected static final String ESCAPED_SCRIPT = "~`";

  private static Map<String, Function> FUNCTIONS = new HashMap<String, Function>();
  static {
    FUNCTIONS.put(EMBEDDED_SCRIPT, new ScriptFunction(EMBEDDED_SCRIPT, "Embedded scripts `...`"));
    FUNCTIONS.put(ESCAPED_SCRIPT, new ScriptFunction(ESCAPED_SCRIPT, "Escaped script ~`...`"));
  }

  public EmbeddedScriptFunctions(ProblemsHandler problemsHandler) {
    super(problemsHandler);
  }

  @Override
  protected Map<String, Function> getFunctions() {
    return FUNCTIONS;
  }

}

class ScriptFunction implements Function {

  private final String name;
  private final String errorName;

  public ScriptFunction(String name, String errorName) {
    this.name = name;
    this.errorName = errorName;
  }

  @Override
  public Expression evaluate(List<Expression> parameters, ProblemsHandler problemsHandler, FunctionExpression call, Expression evaluatedParameter) {
    if (parameters.size()>1)
      problemsHandler.wrongNumberOfArgumentsToFunctionMin(call.getParameter(), call.getName(), 1);

    Expression parameter = parameters.get(0);
    if (!(parameter instanceof EmbeddedScript)) {
      problemsHandler.wrongArgumentTypeToFunction(parameter, name+"...`", parameter.getType(),ASTCssNodeType.EMBEDDED_SCRIPT);
      return new FaultyExpression(call);
    }
    
    warn(call, problemsHandler);
    
    EmbeddedScript parameterAsStr = (EmbeddedScript) parameter;
    String value = name+parameterAsStr.getValue()+"`"; 
    return new CssString(call.getUnderlyingStructure(), value, "");
  }

  private void warn(FunctionExpression call, ProblemsHandler problemsHandler) {
    problemsHandler.warnScriptingNotSupported(call, errorName);
    
  }

  @Override
  public boolean acceptsParameters(List<Expression> parameters) {
    return true;
  }

}
