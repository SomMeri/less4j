package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.ColorExpression;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.EscapedValue;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FaultyExpression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.compiler.expressions.strings.StringFormatter;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.utils.InStringCssPrinter;
import com.github.sommeri.less4j.utils.PrintUtils;

public class StringFunctions extends BuiltInFunctionsPack {

  protected static final String ESCAPE = "escape";
  protected static final String E = "e";
  protected static final String FORMAT = "%";

  private static Map<String, Function> FUNCTIONS = new HashMap<String, Function>();
  static {
    FUNCTIONS.put(ESCAPE, new Escape());
    FUNCTIONS.put(E, new E());
    FUNCTIONS.put(FORMAT, new Format());
  }

  public StringFunctions(ProblemsHandler problemsHandler) {
    super(problemsHandler);
  }

  @Override
  protected Map<String, Function> getFunctions() {
    return FUNCTIONS;
  }

}

class E extends AbstractFunction {

  @Override
  public Expression evaluate(List<Expression> parameters, ProblemsHandler problemsHandler, FunctionExpression call, Expression evaluatedParameter) {
    if (parameters.size()>1)
      problemsHandler.wrongNumberOfArgumentsToFunctionMin(call.getParameter(), call.getName(), 1);

    Expression parameter = parameters.get(0);
    if (parameter.getType()==ASTCssNodeType.STRING_EXPRESSION) 
      return evaluate((CssString) parameter);

    if (parameter.getType()==ASTCssNodeType.ESCAPED_VALUE) 
      return evaluate((EscapedValue) parameter);

    if (parameter.getType()==ASTCssNodeType.NUMBER) 
      return evaluate((NumberExpression) parameter);

    problemsHandler.warnEFunctionArgument(parameter);

    return new FaultyExpression(call.getParameter());
  }

  private Expression evaluate(NumberExpression parameters) {
    InStringCssPrinter printer = new InStringCssPrinter();
    printer.append(parameters);
    return new CssString(parameters.getUnderlyingStructure(), printer.toString(), "");
  }

  private Expression evaluate(EscapedValue parameters) {
    return parameters;
  }

  private CssString evaluate(CssString parameters) {
    return new CssString(parameters.getUnderlyingStructure(), parameters.getValue(), "");
  }

}

class Escape extends AbstractFunction {

  @Override
  public Expression evaluate(List<Expression> parameters, ProblemsHandler problemsHandler, FunctionExpression call, Expression evaluatedParameter) {
    if (parameters.size()>1)
      problemsHandler.wrongNumberOfArgumentsToFunctionMin(call.getParameter(), call.getName(), 1);

    Expression parameter = parameters.get(0);
    if (parameter.getType()==ASTCssNodeType.STRING_EXPRESSION) 
      return evaluate((CssString) parameter);

    if (parameter.getType()==ASTCssNodeType.ESCAPED_VALUE) 
      return evaluate((EscapedValue) parameter);

    problemsHandler.warnEscapeFunctionArgument(call.getParameter());

    if (parameter.getType()==ASTCssNodeType.COLOR_EXPRESSION) 
      return evaluate((ColorExpression) parameter);

    return call.getParameter();
  }

  private Expression evaluate(ColorExpression parameters) {
    return new CssString(parameters.getUnderlyingStructure(), "undefined", "");
  }

  private CssString evaluate(EscapedValue parameters) {
    String newValue = PrintUtils.toUtf8ExceptURL(parameters.getValue());
    return new CssString(parameters.getUnderlyingStructure(), newValue, "");
  }

  private CssString evaluate(CssString parameters) {
    String newValue = PrintUtils.toUtf8ExceptURL(parameters.getValue());
    return new CssString(parameters.getUnderlyingStructure(), newValue, "");
  }

}

class Format extends AbstractFunction {

  @Override
  public Expression evaluate(List<Expression> parameters, ProblemsHandler problemsHandler, FunctionExpression call, Expression evaluatedParameter) {
    if (parameters.isEmpty()) 
      problemsHandler.errFormatWrongFirstParameter(call.getParameter());
    
    Expression format = parameters.get(0);
    if (format.getType()==ASTCssNodeType.STRING_EXPRESSION) 
      return evaluate((CssString) format, parameters.subList(1, parameters.size()), problemsHandler, call.getUnderlyingStructure());

    if (format.getType()==ASTCssNodeType.ESCAPED_VALUE) 
      return evaluate((EscapedValue) format, parameters.subList(1, parameters.size()), problemsHandler, call.getUnderlyingStructure());

    if (!format.isFaulty()) 
        problemsHandler.errFormatWrongFirstParameter(call.getParameter());
    
    return new FaultyExpression(call);
  }

  private Expression evaluate(EscapedValue format, List<Expression> parameters, ProblemsHandler problemsHandler, HiddenTokenAwareTree technicalUnderlying) {
    String newValue = format(format.getValue(), parameters, problemsHandler, technicalUnderlying);
    return new CssString(format.getUnderlyingStructure(), newValue, "\"");
  }

  private Expression evaluate(CssString format, List<Expression> parameters, ProblemsHandler problemsHandler, HiddenTokenAwareTree technicalUnderlying) {
    String newValue = format(format.getValue(), parameters, problemsHandler, technicalUnderlying);
    return new CssString(format.getUnderlyingStructure(), newValue, "\"");
  }

  private String format(String value, List<Expression> parameters, ProblemsHandler problemsHandler, HiddenTokenAwareTree technicalUnderlying) {
    StringFormatter formatter = new StringFormatter(problemsHandler);
    return formatter.replaceIn(value, parameters.iterator(), technicalUnderlying);
  }

}