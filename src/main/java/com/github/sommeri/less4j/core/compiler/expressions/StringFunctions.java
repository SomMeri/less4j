package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.ColorExpression;
import com.github.sommeri.less4j.core.ast.ComposedExpression;
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

public class StringFunctions implements FunctionsPackage {

  protected static final String ESCAPE = "escape";
  protected static final String E = "e";
  protected static final String FORMAT = "%";

  private static Map<String, Function> FUNCTIONS = new HashMap<String, Function>();
  static {
    FUNCTIONS.put(ESCAPE, new Escape());
    FUNCTIONS.put(E, new E());
    FUNCTIONS.put(FORMAT, new Format());
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

class E implements Function {

  @Override
  public Expression evaluate(Expression parameters, ProblemsHandler problemsHandler) {
    if (parameters.getType()==ASTCssNodeType.STRING_EXPRESSION) 
      return evaluate((CssString) parameters);

    if (parameters.getType()==ASTCssNodeType.ESCAPED_VALUE) 
      return evaluate((EscapedValue) parameters);

    if (parameters.getType()==ASTCssNodeType.NUMBER) 
      return evaluate((NumberExpression) parameters);

    problemsHandler.warnEFunctionArgument(parameters);

    return new FaultyExpression(parameters);
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
    String newValue = PrintUtils.toUtf8ExceptURL(parameters.getValue());
    return new CssString(parameters.getUnderlyingStructure(), newValue, "");
  }

  private CssString evaluate(CssString parameters) {
    String newValue = PrintUtils.toUtf8ExceptURL(parameters.getValue());
    return new CssString(parameters.getUnderlyingStructure(), newValue, "");
  }

}

class Format implements Function {

  @Override
  public Expression evaluate(Expression param, ProblemsHandler problemsHandler) {
    
    List<Expression> parameters = splitToParams(param);
    if (parameters.isEmpty()) 
      problemsHandler.errFormatWrongFirstParameter(param);
    
    Expression format = parameters.get(0);
    if (format.getType()==ASTCssNodeType.STRING_EXPRESSION) 
      return evaluate((CssString) format, parameters.subList(1, parameters.size()), param.getUnderlyingStructure());

    if (format.getType()==ASTCssNodeType.ESCAPED_VALUE) 
      return evaluate((EscapedValue) format, parameters.subList(1, parameters.size()), param.getUnderlyingStructure());

    if (!format.isFaulty()) 
        problemsHandler.errFormatWrongFirstParameter(param);
    
    return new FaultyExpression(param);
  }

  private Expression evaluate(EscapedValue format, List<Expression> parameters, HiddenTokenAwareTree technicalUnderlying) {
    String newValue = format(format.getValue(), parameters, technicalUnderlying);
    return new CssString(format.getUnderlyingStructure(), newValue, "\"");
  }

  private Expression evaluate(CssString format, List<Expression> parameters, HiddenTokenAwareTree technicalUnderlying) {
    String newValue = format(format.getValue(), parameters, technicalUnderlying);
    return new CssString(format.getUnderlyingStructure(), newValue, "\"");
  }

  private String format(String value, List<Expression> parameters, HiddenTokenAwareTree technicalUnderlying) {
    StringFormatter formatter = new StringFormatter();
    return formatter.replaceIn(value, parameters.iterator(), technicalUnderlying);
  }

  private List<Expression> splitToParams(Expression param) {
    if (param==null) 
      return Collections.emptyList();
    
    //TODO: this may stop to work after arguments changes in less.js-1.4.0
    if (param instanceof ComposedExpression) {
      ComposedExpression composed = (ComposedExpression) param;
      return composed.splitByComma();
    }
    
    return Arrays.asList(param);
  }

}