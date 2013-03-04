package com.github.sommeri.less4j.core.compiler.expressions;

import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.EscapedValue;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.platform.Constants;

public class TypesConversionUtils {

  public Double toDouble(CssString input) {
    try{
      return Double.parseDouble(input.getValue());
    } catch (NumberFormatException ex) {
      return Double.NaN;
    }
  }

  public String contentToString(Expression input) {
    switch (input.getType()) {
    case IDENTIFIER_EXPRESSION:
      return ((IdentifierExpression)input).getValue();

    case STRING_EXPRESSION:
      return ((CssString)input).getValue();

    case ESCAPED_VALUE:
      return ((EscapedValue)input).getValue();

    default:
      return null;
    }
  }

  public String extractFilename(Expression urlInput, ProblemsHandler problemsHandler) {
    ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(problemsHandler);
    Expression urlExpression = expressionEvaluator.evaluate(urlInput);

    if (urlExpression.getType() != ASTCssNodeType.FUNCTION)
      return toJavaFileSeparator(contentToString(urlExpression));

    //this is the only place in the compiler that can interpret the url function
    FunctionExpression function = (FunctionExpression) urlExpression;
    if (!"url".equals(function.getName().toLowerCase()))
      return null;

    return toJavaFileSeparator(contentToString(expressionEvaluator.evaluate(function.getParameter())));
  }

  private String toJavaFileSeparator(String path) {
    if (Constants.FILE_SEPARATOR.equals("/")) {
      return path;
    }

    return path.replace(Constants.FILE_SEPARATOR, "/");
  }

}
