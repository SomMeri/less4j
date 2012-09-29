package com.github.less4j.core.parser;

import java.util.List;

import org.porting.less4j.core.parser.LessLexer;

import com.github.less4j.core.ast.ColorExpression;
import com.github.less4j.core.ast.CssString;
import com.github.less4j.core.ast.Expression;
import com.github.less4j.core.ast.FunctionExpression;
import com.github.less4j.core.ast.IdentifierExpression;
import com.github.less4j.core.ast.IndirectVariable;
import com.github.less4j.core.ast.NamedColorExpression;
import com.github.less4j.core.ast.NamedExpression;
import com.github.less4j.core.ast.NumberExpression;
import com.github.less4j.core.ast.ParenthesesExpression;
import com.github.less4j.core.ast.SignedExpression;
import com.github.less4j.core.ast.Variable;
import com.github.less4j.core.ast.NumberExpression.Dimension;
import com.github.less4j.platform.Constants;
import com.github.less4j.utils.PrintUtils;

public class TermBuilder {

  private final ASTBuilderSwitch parentBuilder;

  public TermBuilder(ASTBuilderSwitch astBuilderSwitch) {
    this.parentBuilder = astBuilderSwitch;
  }

  public Expression buildFromTerm(HiddenTokenAwareTree token) {
    return buildFromTerm(token, 0);
  }

  public Expression buildFromTerm(HiddenTokenAwareTree token, int offsetChildIndx) {
    HiddenTokenAwareTree offsetChild = token.getChildren().get(offsetChildIndx);
    switch (offsetChild.getType()) {
    case LessLexer.IDENT:
      return buildFromIdentifier(token, offsetChild);

    case LessLexer.STRING:
      return buildFromString(token, offsetChild);

    case LessLexer.HASH:
      return buildFromColorHash(token, offsetChild);

    case LessLexer.PLUS:
    case LessLexer.MINUS:
      return negate(buildFromTerm(token, offsetChildIndx + 1), offsetChild);

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
      return buildFromSpecialFunction(token, offsetChild);

    case LessLexer.TERM_FUNCTION:
      return buildFromNormalFunction(token, offsetChild);

    case LessLexer.VARIABLE:
      return buildFromVariable(token, offsetChild);

    case LessLexer.INDIRECT_VARIABLE:
      return buildFromIndirectVariable(token, offsetChild);

    case LessLexer.TERM:
      return buildFromTerm(offsetChild);

    case LessLexer.EXPRESSION_PARENTHESES:
      return buildFromParentheses(offsetChild);

    default:
      throw new IncorrectTreeException("type number: " + PrintUtils.toName(offsetChild.getType()) + "(" + offsetChild.getType() + ") for " + offsetChild.getText(), offsetChild);

    }
  }

  private Expression buildFromParentheses(HiddenTokenAwareTree first) {
    // FIXME: test comments!
    first.addBeforeFollowing(first.getLastChild().getFollowing());
    return new ParenthesesExpression(first, parentBuilder.handleExpression(first.getChild(1)));
  }

  private Expression buildFromColorHash(HiddenTokenAwareTree token, HiddenTokenAwareTree first) {
    return new ColorExpression(token, first.getText());
  }

  private Expression negate(Expression value, HiddenTokenAwareTree sign) {
    if (value instanceof NumberExpression) {
      NumberExpression number = (NumberExpression) value;
      number.setExpliciteSign(true);

      if (sign.getType() == LessLexer.MINUS) {
        number.negate();
        number.setOriginalString("-" + number.getOriginalString());
      } else if (sign.getType() == LessLexer.PLUS) {
        number.setOriginalString("+" + number.getOriginalString());
      }

      return number;
    }

    if (sign.getType() == LessLexer.MINUS) {
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
    String numberPart = value.replaceAll("[a-z%]", "");
    result.setValueAsDouble(Double.valueOf(numberPart));
    if (numberPart.length() < value.length())
      result.setSuffix(value.substring(numberPart.length()));
    else
      result.setSuffix("");
  }

  private Dimension toDimension(HiddenTokenAwareTree actual) {
    switch (actual.getType()) {
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
      throw new IncorrectTreeException("Unknown dimension type: " + actual.getType() + " " + actual.getText(), actual);

    }
  }

  private Expression buildFromString(HiddenTokenAwareTree token, HiddenTokenAwareTree first) {
    String text = first.getText();
    return createCssString(token, text);
  }

  public CssString createCssString(HiddenTokenAwareTree token, String quotedText) {
    return new CssString(token, quotedText.substring(1, quotedText.length() - 1), quotedText.substring(0, 1));
  }

  private Expression buildFromIdentifier(HiddenTokenAwareTree parent, HiddenTokenAwareTree first) {
    String text = first.getText();
    if (NamedColorExpression.isColorName(text))
      return new NamedColorExpression(parent, text);

    return new IdentifierExpression(parent, text);
  }

  private FunctionExpression buildFromSpecialFunction(HiddenTokenAwareTree token, HiddenTokenAwareTree first) {
    return new FunctionExpression(token, "url", extractUrlParameter(token, normalizeNewLineSymbols(first.getText())));
  }

  // some places (e.g. only url) allow new lines in them. Less.js tend to
  // translate them into
  private String normalizeNewLineSymbols(String text) {
    return text.replaceAll("\r?\n", Constants.NEW_LINE);
  }

  private Expression extractUrlParameter(HiddenTokenAwareTree token, String text) {
    if (text == null)
      return null;

    if (text.length() < 5)
      return new CssString(token, "", "");

    String string = text.substring(4, text.length() - 1);
    return new CssString(token, string, "");
  }

  private FunctionExpression buildFromNormalFunction(HiddenTokenAwareTree token, HiddenTokenAwareTree actual) {
    List<HiddenTokenAwareTree> children = actual.getChildren();
    String name = children.get(0).getText();
    HiddenTokenAwareTree parameterNode = children.get(1);

    if (parameterNode.getType() != LessLexer.OPEQ) {
      Expression parameter = (Expression) parentBuilder.switchOn(parameterNode);
      return new FunctionExpression(token, name, parameter);
    }

    //first child is a name
    HiddenTokenAwareTree parameterName = parameterNode.getChild(0);
    HiddenTokenAwareTree parameterValue = parameterNode.getChild(1);
    Expression parameter = (Expression) parentBuilder.switchOn(parameterValue);

    return new FunctionExpression(token, name, new NamedExpression(parameterNode, parameterName.getText(), parameter));
  }

  public Variable buildFromVariable(HiddenTokenAwareTree variableToken) {
    return buildFromVariable(null, variableToken);
  }

  private Variable buildFromVariable(HiddenTokenAwareTree expressionToken, HiddenTokenAwareTree variableToken) {
    if (expressionToken != null) {
      expressionToken.addFollowing(variableToken.getFollowing());
      return new Variable(expressionToken, variableToken.getText());
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
