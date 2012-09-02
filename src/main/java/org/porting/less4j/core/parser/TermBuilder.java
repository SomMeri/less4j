package org.porting.less4j.core.parser;

import java.util.List;

import org.porting.less4j.core.ast.ColorExpression;
import org.porting.less4j.core.ast.CssString;
import org.porting.less4j.core.ast.Expression;
import org.porting.less4j.core.ast.FunctionExpression;
import org.porting.less4j.core.ast.IdentifierExpression;
import org.porting.less4j.core.ast.NamedColorExpression;
import org.porting.less4j.core.ast.NamedExpression;
import org.porting.less4j.core.ast.NumberExpression;
import org.porting.less4j.core.ast.NumberExpression.Dimension;
import org.porting.less4j.core.ast.NumberExpression.Sign;
import org.porting.less4j.platform.Constants;

public class TermBuilder {

  private final ASTBuilderSwitch parentBuilder;

  public TermBuilder(ASTBuilderSwitch astBuilderSwitch) {
    this.parentBuilder = astBuilderSwitch;
  }

  public Expression buildFromTerm(HiddenTokenAwareTree token) {
    List<HiddenTokenAwareTree> children = token.getChildren();
    HiddenTokenAwareTree first = children.get(0);
    switch (first.getType()) {
    case LessLexer.IDENT:
      return buildFromIdentifier(token, first);

    case LessLexer.STRING:
      return buildFromString(token, first);

    case LessLexer.HASH:
      return buildFromColorHash(token, first);

    case LessLexer.PLUS:
    case LessLexer.MINUS:
      return buildFromSignedNumber(token, children);

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
      return buildFromNumber(token, first);

    case LessLexer.URI:
      return buildFromSpecialFunction(token, first);

    case LessLexer.TERM_FUNCTION:
      return buildFromNormalFunction(token, first);

    case LessLexer.TERM:
      return buildFromTerm(first);
    }

    throw new IncorrectTreeException("type number: " + first.getType() + " for " + first.getText());
  }

  private Expression buildFromColorHash(HiddenTokenAwareTree token, HiddenTokenAwareTree first) {
    return new ColorExpression(token, first.getText());
  }

  private Expression buildFromSignedNumber(HiddenTokenAwareTree token, List<HiddenTokenAwareTree> children) {
    HiddenTokenAwareTree sign = children.get(0);
    NumberExpression number = buildFromNumber(token, children.get(1));
    if (sign.getType() == LessLexer.PLUS)
      number.setSign(Sign.PLUS);
    if (sign.getType() == LessLexer.MINUS)
      number.setSign(Sign.MINUS);

    return number;
  }

  private NumberExpression buildFromNumber(HiddenTokenAwareTree token, HiddenTokenAwareTree actual) {
    NumberExpression result = new NumberExpression(token);
    result.setValueAsString(actual.getText());
    result.setDimension(toDimension(actual));
    return result;
  }

  private Dimension toDimension(HiddenTokenAwareTree actual) {
    switch (actual.getType()) {
    case LessLexer.NUMBER:
      return Dimension.NUMBER;
    case LessLexer.PERCENTAGE:
      return Dimension.PERCENTAGE;
    case LessLexer.UNKNOWN_DIMENSION:
      return Dimension.NONE;
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
      return Dimension.NONE;

    }
  }

  // FIXME: the current grammar does not support escaping
  private Expression buildFromString(HiddenTokenAwareTree token, HiddenTokenAwareTree first) {
    return new CssString(token, first.getText());
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
      return new CssString(token, "");

    return new CssString(token, text.substring(4, text.length() - 1));
  }

  // FIXME: write test for this, there is no such case
  private FunctionExpression buildFromNormalFunction(HiddenTokenAwareTree token, HiddenTokenAwareTree first) {
    List<HiddenTokenAwareTree> children = first.getChildren();
    String name = children.get(0).getText();
    HiddenTokenAwareTree parameterNode = children.get(1);

    //TODO not sure if clean
    if (parameterNode.getType()!=LessLexer.OPEQ) {
      Expression parameter = (Expression) parentBuilder.switchOn(parameterNode);
      return new FunctionExpression(token, name, parameter);
    }
    
    //first child is a name
    HiddenTokenAwareTree parameterName = parameterNode.getChild(0);
    HiddenTokenAwareTree parameterValue = parameterNode.getChild(1);
    Expression parameter = (Expression) parentBuilder.switchOn(parameterValue);
    
    return new FunctionExpression(token, name, new NamedExpression(parameterNode, parameterName.getText(), parameter));
  }

}
