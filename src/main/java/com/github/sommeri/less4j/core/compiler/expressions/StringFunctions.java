package com.github.sommeri.less4j.core.compiler.expressions;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.ColorExpression;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.EscapedValue;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class StringFunctions implements FunctionsPackage {

  protected static final String ESCAPE = "escape";

  private static Map<String, Function> FUNCTIONS = new HashMap<String, Function>();
  static {
    FUNCTIONS.put(ESCAPE, new Escape());
  }

  private final ProblemsHandler problemsHandler;

  public StringFunctions(ProblemsHandler problemsHandler) {
    this.problemsHandler = problemsHandler;
  }

  public boolean canEvaluate(FunctionExpression input, Expression parameters) {
    return FUNCTIONS.containsKey(input.getName());
  }
  
  public Expression evaluate(FunctionExpression input, Expression parameters) {
    if (!canEvaluate(input, parameters))
      return input;

    Function function = FUNCTIONS.get(input.getName());
    return function.evaluate(parameters, problemsHandler);
  }

}

class Escape implements Function {

  @Override
  public Expression evaluate(Expression parameters, ProblemsHandler problemsHandler) {
    if (parameters.getType()==ASTCssNodeType.STRING_EXPRESSION) 
      return evaluate((CssString) parameters);

    if (parameters.getType()==ASTCssNodeType.ESCAPED_VALUE) 
      return evaluate((EscapedValue) parameters);

    problemsHandler.warnEscapeFunctionArgument(parameters);

    if (parameters.getType()==ASTCssNodeType.COLOR_EXPRESSION) 
      return evaluate((ColorExpression) parameters);

    return parameters;
  }

  private Expression evaluate(ColorExpression parameters) {
    return new CssString(parameters.getUnderlyingStructure(), "undefined", "");
  }

  private CssString evaluate(EscapedValue parameters) {
    String newValue = escape(parameters.getValue());
    return new CssString(parameters.getUnderlyingStructure(), newValue, "");
  }

  private CssString evaluate(CssString parameters) {
    String newValue = escape(parameters.getValue());
    return new CssString(parameters.getUnderlyingStructure(), newValue, "");
  }

  private String escape(String value) {
    try {
      String encode = URLEncoder.encode(value, "UTF-8");
      encode = encode.replaceAll("\\+", "%20").replaceAll("%2C", ",").replaceAll("%2F", "/").replaceAll("%3F", "?");
      encode = encode.replaceAll("%40", "@").replaceAll("%26", "&").replaceAll("%2B", "+");
      encode = encode.replaceAll("%27", "'").replaceAll("%7E", "~").replaceAll("%21", "!");
      encode = encode.replace("%24", "$"); //replaceAll crash on this
      return encode;
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException(e);
    }
  }
  
}