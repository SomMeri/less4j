package com.github.sommeri.less4j.core.parser;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.ColorExpression;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.EmbeddedScript;
import com.github.sommeri.less4j.core.ast.EmptyExpression;
import com.github.sommeri.less4j.core.ast.EscapedValue;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FaultyExpression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.ast.IndirectVariable;
import com.github.sommeri.less4j.core.ast.KeywordExpression;
import com.github.sommeri.less4j.core.ast.ListExpression;
import com.github.sommeri.less4j.core.ast.ListExpressionOperator;
import com.github.sommeri.less4j.core.ast.NamedColorExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression.Dimension;
import com.github.sommeri.less4j.core.ast.ParenthesesExpression;
import com.github.sommeri.less4j.core.ast.SignedExpression;
import com.github.sommeri.less4j.core.ast.UnicodeRangeExpression;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.problems.BugHappened;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.platform.Constants;
import com.github.sommeri.less4j.utils.PrintUtils;
import com.github.sommeri.less4j.utils.URIUtils;

public class TermBuilder {

  private static final String URL = "url";
  private final ASTBuilderSwitch parentBuilder;
  private ProblemsHandler problemsHandler;

  public TermBuilder(ASTBuilderSwitch astBuilderSwitch, ProblemsHandler problemsHandler) {
    this.parentBuilder = astBuilderSwitch;
    this.problemsHandler = problemsHandler;
  }

  public Expression buildFromTerm(HiddenTokenAwareTree token) {
    return buildFromTerm(token, token, 0);
  }

  public Expression buildFromChildTerm(HiddenTokenAwareTree token) {
    return buildFromChildTerm(token, 0);
  }

  private Expression buildFromChildTerm(HiddenTokenAwareTree token, int offsetChildIndx) {
    HiddenTokenAwareTree offsetChild = token.getChildren().get(offsetChildIndx);
    return buildFromTerm(token, offsetChild, offsetChildIndx);
  }

  private Expression buildFromTerm(HiddenTokenAwareTree token, HiddenTokenAwareTree offsetChild, int offsetChildIndx) {
    switch (offsetChild.getGeneralType()) {
    case LessLexer.IDENT_TERM:
      return buildFromLongIdentifier(token, offsetChild);
      
    case LessLexer.IDENT:
    case LessLexer.PERCENT:
      return buildFromPercent(token, offsetChild);

    case LessLexer.STRING:
      return buildFromString(token, offsetChild);

    case LessLexer.HASH:
      return buildFromColorHash(token, offsetChild);

    case LessLexer.PLUS:
    case LessLexer.MINUS:
      return negate(buildFromChildTerm(token, offsetChildIndx + 1), offsetChild);

    case LessLexer.NUMBER:
    case LessLexer.PERCENTAGE:
    case LessLexer.UNKNOWN_DIMENSION:
    case LessLexer.REPEATER:
    case LessLexer.LENGTH:
    case LessLexer.EMS:
    case LessLexer.EXS:
    case LessLexer.ANGLE:
    case LessLexer.TIME:
    case LessLexer.FREQ:
      return buildFromNumber(token, offsetChild);

    case LessLexer.URI:
      return buildFromSpecialFunction(token, URL, offsetChild);

    case LessLexer.DOMAIN:
      return buildFromSpecialFunction(token, "domain", offsetChild);

    case LessLexer.URL_PREFIX:
      return buildFromSpecialFunction(token, "url-prefix", offsetChild);

    case LessLexer.TERM_FUNCTION:
      return buildFromNormalFunction(token, offsetChild);

    case LessLexer.AT_NAME:
      return buildFromVariable(token, offsetChild);

    case LessLexer.INDIRECT_VARIABLE:
      return buildFromIndirectVariable(token, offsetChild);

    case LessLexer.TERM:
      return buildFromChildTerm(offsetChild);

    case LessLexer.EXPRESSION_PARENTHESES:
      return buildFromParentheses(offsetChild);

    case LessLexer.ESCAPED_VALUE:
      return buildFromEscapedValue(token, offsetChild);

    case LessLexer.UNICODE_RANGE:
      return buildFromUnicodeRange(token, offsetChild);

    case LessLexer.ESCAPED_SCRIPT:
      return buildFromEscapedScript(token, offsetChild);

    case LessLexer.EMBEDDED_SCRIPT:
      return buildFromEmbeddedScript(token, offsetChild);

    default:
      throw new BugHappened("type number: " + PrintUtils.toName(offsetChild.getGeneralType()) + "(" + offsetChild.getGeneralType() + ") for " + offsetChild.getText(), offsetChild);

    }
  }

  private Expression buildFromParentheses(HiddenTokenAwareTree first) {
    first.addBeforeFollowing(first.getLastChild().getFollowing());
    return new ParenthesesExpression(first, parentBuilder.handleExpression(first.getChild(1)));
  }

  private Expression buildFromColorHash(HiddenTokenAwareTree token, HiddenTokenAwareTree first) {
    String text = first.getText();
    ColorExpression parsedColor = ConversionUtils.parseColor(token, text);
    if (parsedColor == null) {
      FaultyExpression faultyExpression = new FaultyExpression(token);
      problemsHandler.notAColor(faultyExpression, text);
      return faultyExpression;
    }

    return parsedColor;
  }

  private Expression negate(Expression value, HiddenTokenAwareTree sign) {
    if (value instanceof NumberExpression) {
      NumberExpression number = (NumberExpression) value;
      number.setExpliciteSign(true);

      if (sign.getGeneralType() == LessLexer.MINUS) {
        number.negate();
        number.setOriginalString("-" + number.getOriginalString());
      } else if (sign.getGeneralType() == LessLexer.PLUS) {
        number.setOriginalString("+" + number.getOriginalString());
      }

      return number;
    }

    if (sign.getGeneralType() == LessLexer.MINUS) {
      return new SignedExpression(sign, SignedExpression.Sign.MINUS, value);
    }

    return new SignedExpression(sign, SignedExpression.Sign.PLUS, value);
  }

  private NumberExpression buildFromNumber(HiddenTokenAwareTree token, HiddenTokenAwareTree actual) {
    NumberExpression result = new NumberExpression(token);
    String valueAsString = actual.getText().trim();
    setDoubleAndType(result, valueAsString);
    result.setOriginalString(valueAsString);
    result.setDimension(toDimension(actual));
    return result;
  }

  private void setDoubleAndType(NumberExpression result, String value) {
    value = value.toLowerCase().trim();
    String numberPart = value.replaceAll("[^0-9\\.]*", "");
    result.setValueAsDouble(Double.valueOf(numberPart));
    if (numberPart.length() < value.length())
      result.setSuffix(value.substring(numberPart.length()));
    else
      result.setSuffix("");
  }

  private Dimension toDimension(HiddenTokenAwareTree actual) {
    switch (actual.getGeneralType()) {
    case LessLexer.NUMBER:
      return Dimension.NUMBER;
    case LessLexer.PERCENTAGE:
      return Dimension.PERCENTAGE;
    case LessLexer.UNKNOWN_DIMENSION:
      return Dimension.UNKNOWN;
    case LessLexer.REPEATER:
      return Dimension.REPEATER;
    case LessLexer.LENGTH:
      return Dimension.LENGTH;
    case LessLexer.EMS:
      return Dimension.EMS;
    case LessLexer.EXS:
      return Dimension.EXS;
    case LessLexer.ANGLE:
      return Dimension.ANGLE;
    case LessLexer.TIME:
      return Dimension.TIME;
    case LessLexer.FREQ:
      return Dimension.FREQ;

    default:
      throw new BugHappened("Unknown dimension type: " + actual.getGeneralType() + " " + actual.getText(), actual);

    }
  }

  public EscapedValue buildFromEscapedValue(HiddenTokenAwareTree token, HiddenTokenAwareTree offsetChild) {
    token.pushHiddenToKids();
    offsetChild.pushHiddenToKids();
    HiddenTokenAwareTree valueToken = offsetChild.getChild(0);
    String quotedText = valueToken.getText();
    return new EscapedValue(valueToken, quotedText.substring(2, quotedText.length() - 1), quotedText.substring(1, 1));
  }

  private Expression buildFromString(HiddenTokenAwareTree token, HiddenTokenAwareTree first) {
    String text = first.getText();
    return createCssString(token, text);
  }

  public CssString createCssString(HiddenTokenAwareTree token, String quotedText) {
    return new CssString(token, quotedText.substring(1, quotedText.length() - 1), quotedText.substring(0, 1));
  }

  private Expression buildFromEscapedScript(HiddenTokenAwareTree token, HiddenTokenAwareTree first) {
    String text = first.getText();
    text = text.substring(2, text.length() - 1);
    return new FunctionExpression(token, "~`", new EmbeddedScript(token, text));
  }

  private Expression buildFromEmbeddedScript(HiddenTokenAwareTree token, HiddenTokenAwareTree first) {
    String text = first.getText();
    text = text.substring(1, text.length() - 1);
    return new FunctionExpression(token, "`", new EmbeddedScript(token, text));
  }

  private Expression buildFromPercent(HiddenTokenAwareTree parent, HiddenTokenAwareTree first) {
    String text = first.getText();
    return createIdentifierExpression(parent, text);
  }

  private Expression buildFromLongIdentifier(HiddenTokenAwareTree parent, HiddenTokenAwareTree first) {
    StringBuilder text = new StringBuilder();
    List<HiddenTokenAwareTree> children = first.getChildren();
    for (HiddenTokenAwareTree child : children) {
      text.append(child.getText().trim());
    }
    
    if (children.size()==1 && children.get(0).getGeneralType() == LessLexer.IMPORTANT_SYM) {
      return new KeywordExpression(parent, children.get(0).getText().trim(), true);
    }
    
    return createIdentifierExpression(parent, text.toString());
  }

  private Expression createIdentifierExpression(HiddenTokenAwareTree parent, String text) {
    if (NamedColorExpression.isColorName(text))
      return NamedColorExpression.createNamedColorExpression(parent, text);

    return new IdentifierExpression(parent, text);
  }

  private Expression buildFromUnicodeRange(HiddenTokenAwareTree parent, HiddenTokenAwareTree first) {
    return new UnicodeRangeExpression(parent, first.getText());
  }

  private FunctionExpression buildFromSpecialFunction(HiddenTokenAwareTree token, String function, HiddenTokenAwareTree first) {
    Expression parameter = extractUrlParameter(token, function, normalizeNewLineSymbols(first.getText()));
    parameter = packIntoListExpression(parameter);
    return new FunctionExpression(token, function, parameter);
  }

  // some places (e.g. only url) allow new lines in them. Less.js tend to
  // translate them into
  private String normalizeNewLineSymbols(String text) {
    return text.replaceAll("\r?\n", Constants.NEW_LINE);
  }

  private Expression extractUrlParameter(HiddenTokenAwareTree token, String function, String text) {
    if (text == null)
      return null;

    if (text.length() <= function.length() + 2)
      return new CssString(token, "", "");

    String string = text.substring(function.length() + 1, text.length() - 1);
    if (!URIUtils.isQuotedUrl(string))
      return new CssString(token, string, "");

    String quote = String.valueOf(string.charAt(0));
    return new CssString(token, string.substring(1, string.length() - 1), quote);
  }

  private FunctionExpression buildFromNormalFunction(HiddenTokenAwareTree token, HiddenTokenAwareTree actual) {
    List<HiddenTokenAwareTree> children = actual.getChildren();
    String name = buildFunctionName(children.get(0));

    if (children.size() == 1) {
      /* No arguments to the function */
      return new FunctionExpression(token, name, new EmptyExpression(token));
    }

    HiddenTokenAwareTree parameterNode = children.get(1);

    Expression parameter = (Expression) parentBuilder.switchOn(parameterNode);
    //FIXME: (API) this is a hack - if what come out is not comma separated list, add it to comma separated list. - once there is API changing version it will be better to store parameters list in the function
    if (!isListOfParameters(parameter)) {
      parameter = packIntoListExpression(parameter);
    }
    return new FunctionExpression(token, name, parameter);
  }

  private Expression packIntoListExpression(Expression parameter) {
    ListExpressionOperator operator = new ListExpressionOperator(parameter.getUnderlyingStructure(), ListExpressionOperator.Operator.COMMA);
    return new ListExpression(parameter.getUnderlyingStructure(), asList(parameter), operator);
  }

  private List<Expression> asList(Expression... parameter) {
    List<Expression> result = new ArrayList<Expression>();
    for (Expression expression : parameter) {
      result.add(expression);
    }
    return result;
  }

  private boolean isListOfParameters(Expression parameter) {
    if (parameter.getType()!=ASTCssNodeType.LIST_EXPRESSION)
      return false;
    
    ListExpression list = (ListExpression)parameter;
    return list.getOperator().getOperator()==ListExpressionOperator.Operator.COMMA;
  }

  private String buildFunctionName(HiddenTokenAwareTree token) {
    String result = "";
    for (HiddenTokenAwareTree kid : token.getChildren()) {
      result += kid.getText();
    }

    return result;
  }

  public Variable buildFromVariable(HiddenTokenAwareTree variableToken) {
    return buildFromVariable(null, variableToken);
  }

  private Variable buildFromVariable(HiddenTokenAwareTree realOwner, HiddenTokenAwareTree variableToken) {
    if (realOwner != null) {
      realOwner.addFollowing(variableToken.getFollowing());
      return new Variable(realOwner, variableToken.getText());
    }
    return new Variable(variableToken, variableToken.getText());
  }

  public IndirectVariable buildFromIndirectVariable(HiddenTokenAwareTree variableToken) {
    return buildFromIndirectVariable(null, variableToken);
  }

  private IndirectVariable buildFromIndirectVariable(HiddenTokenAwareTree expressionToken, HiddenTokenAwareTree variableToken) {
    if (expressionToken != null) {
      expressionToken.addFollowing(variableToken.getFollowing());
      return new IndirectVariable(expressionToken, variableToken.getText().substring(1));
    }
    return new IndirectVariable(variableToken, variableToken.getText().substring(1));
  }
}
