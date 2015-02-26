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
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.compiler.expressions.strings.StringFormatter;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.js.JsRegExp;
import com.github.sommeri.less4j.utils.InStringCssPrinter;
import com.github.sommeri.less4j.utils.PrintUtils;

public class StringFunctions extends BuiltInFunctionsPack {

  protected static final String ESCAPE = "escape";
  protected static final String E = "e";
  protected static final String FORMAT = "%";
  protected static final String REPLACE = "replace";

  private static Map<String, Function> FUNCTIONS = new HashMap<String, Function>();
  static {
    FUNCTIONS.put(ESCAPE, new Escape());
    FUNCTIONS.put(E, new E());
    FUNCTIONS.put(FORMAT, new Format());
    FUNCTIONS.put(REPLACE, new Replace());
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
    if (parameters.size() > 1)
      problemsHandler.wrongNumberOfArgumentsToFunctionMin(call.getParameter(), call.getName(), 1);

    Expression parameter = parameters.get(0);
    if (parameter.getType() == ASTCssNodeType.STRING_EXPRESSION)
      return evaluate((CssString) parameter);

    if (parameter.getType() == ASTCssNodeType.ESCAPED_VALUE)
      return evaluate((EscapedValue) parameter);

    if (parameter.getType() == ASTCssNodeType.NUMBER)
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
    if (parameters.size() > 1)
      problemsHandler.wrongNumberOfArgumentsToFunctionMax(call.getParameter(), call.getName(), 1);

    Expression parameter = parameters.get(0);
    if (parameter.getType() == ASTCssNodeType.STRING_EXPRESSION)
      return evaluate((CssString) parameter);

    if (parameter.getType() == ASTCssNodeType.ESCAPED_VALUE)
      return evaluate((EscapedValue) parameter);

    problemsHandler.warnEscapeFunctionArgument(call.getParameter());

    if (parameter.getType() == ASTCssNodeType.COLOR_EXPRESSION)
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
    if (format.getType() == ASTCssNodeType.STRING_EXPRESSION)
      return evaluate((CssString) format, parameters.subList(1, parameters.size()), problemsHandler, call.getUnderlyingStructure());

    if (format.getType() == ASTCssNodeType.ESCAPED_VALUE)
      return evaluate((EscapedValue) format, parameters.subList(1, parameters.size()), problemsHandler, call.getUnderlyingStructure());

    if (!format.isFaulty())
      problemsHandler.errFormatWrongFirstParameter(call.getParameter());

    return new FaultyExpression(call);
  }

  private Expression evaluate(EscapedValue format, List<Expression> parameters, ProblemsHandler problemsHandler, HiddenTokenAwareTree technicalUnderlying) {
    String newValue = format(format.getValue(), parameters, problemsHandler, technicalUnderlying);
    return new CssString(format.getUnderlyingStructure(), newValue, format.getQuoteType());
  }

  private Expression evaluate(CssString format, List<Expression> parameters, ProblemsHandler problemsHandler, HiddenTokenAwareTree technicalUnderlying) {
    String newValue = format(format.getValue(), parameters, problemsHandler, technicalUnderlying);
    return new CssString(format.getUnderlyingStructure(), newValue, format.getQuoteType());
  }

  private String format(String value, List<Expression> parameters, ProblemsHandler problemsHandler, HiddenTokenAwareTree technicalUnderlying) {
    StringFormatter formatter = new StringFormatter(problemsHandler);
    return formatter.replaceIn(value, parameters.iterator(), technicalUnderlying);
  }

}

class Replace extends AbstractMultiParameterFunction {

  private TypesConversionUtils conversions = new TypesConversionUtils();

  @Override
  public Expression evaluate(List<Expression> parameters, ProblemsHandler problemsHandler, FunctionExpression call, Expression evaluatedParameter) {
    Expression targetExpression = parameters.get(0);
    String string = conversions.contentToString(targetExpression);
    String pattern = conversions.contentToString(parameters.get(1));
    String replacement = conversions.contentToString(parameters.get(2));
    String flags = parameters.size() > 3 ? conversions.contentToString(parameters.get(3)) : "";

    Expression replaced = regexp(targetExpression, string, pattern, replacement, flags, problemsHandler, call);
    return replaced;
  }

  private Expression regexp(Expression targetExpression, String string, String pattern, String replacement, String flags, ProblemsHandler problemsHandler, FunctionExpression call) {
    try {
      JsRegExp exp = JsRegExp.compile(pattern, flags);
      String replaced = exp.replace(string, replacement);
      return buildResult(targetExpression, replaced);
    } catch (IllegalArgumentException ex) {
      problemsHandler.regexpFunctionError(call, ex.getMessage());
      return new FaultyExpression(call.getUnderlyingStructure());
    }
  }

  private Expression buildResult(Expression targetExpression, String replaced) {
    HiddenTokenAwareTree token = targetExpression.getUnderlyingStructure();
    switch (targetExpression.getType()) {
    case IDENTIFIER_EXPRESSION:
      return new IdentifierExpression(token, replaced);

    case STRING_EXPRESSION:
      CssString string = (CssString) targetExpression;
      return new CssString(token, replaced, string.getQuoteType());

    case ESCAPED_VALUE:
      return new EscapedValue(token, replaced, ((EscapedValue)targetExpression).getQuoteType());

    default:
      return new CssString(token, replaced, "'");
    }
  }

  @Override
  protected int getMinParameters() {
    return 3;
  }

  @Override
  protected int getMaxParameters() {
    return 4;
  }

  @Override
  protected String getName() {
    return StringFunctions.REPLACE;
  }

  @Override
  protected boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler) {
    return conversions.canConvertToString(parameter);
  }

  //  @Override
  //  public Expression evaluate(List<Expression> parameters, ProblemsHandler problemsHandler, FunctionExpression call, Expression evaluatedParameter) {
  //    if (parameters.size()>4)
  //      problemsHandler.wrongNumberOfArgumentsToFunctionMax(call.getParameter(), call.getName(), 4);
  //
  //    if (parameters.size()<3)
  //      problemsHandler.wrongNumberOfArgumentsToFunctionMin(call.getParameter(), call.getName(), 3);
  //  }

}
