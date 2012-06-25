package org.porting.less4j.core.parser;

import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.porting.less4j.core.ast.ColorExpression;
import org.porting.less4j.core.ast.CssString;
import org.porting.less4j.core.ast.Expression;
import org.porting.less4j.core.ast.FunctionExpression;
import org.porting.less4j.core.ast.IdentifierExpression;
import org.porting.less4j.core.ast.NumberExpression;
import org.porting.less4j.core.ast.NumberExpression.Sign;

public class ExpressionBuilder {

  public ExpressionBuilder() {
    super();
  }

  public Expression buildExpression(CommonTree token) {
    List<CommonTree> children = getChildren(token);
    CommonTree first = children.get(0);
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
    }

    throw new IncorrectTreeException("type number: " + first.getType()+ " for " + first.getText());
  }

  private Expression buildFromColorHash(CommonTree token, CommonTree first) {
    return new ColorExpression(token, first.getText());
  }

  private Expression buildFromSignedNumber(CommonTree token, List<CommonTree> children) {
    CommonTree sign = children.get(0);
    NumberExpression number = buildFromNumber(token, children.get(1));
    if (sign.getType()==LessLexer.PLUS)
      number.setSign(Sign.PLUS);
    if (sign.getType()==LessLexer.MINUS)
      number.setSign(Sign.MINUS);
    
    return number;
  }

  private NumberExpression buildFromNumber(CommonTree token, CommonTree actual) {
    NumberExpression result = new NumberExpression(token);
    result.setValueAsString(actual.getText());
    return result;
  }

  // FIXME: the current grammar does not support escaping
  private Expression buildFromString(CommonTree token, CommonTree first) {
    return new CssString(token, first.getText());
  }

  private Expression buildFromIdentifier(CommonTree parent, CommonTree first) {
    return new IdentifierExpression(parent, first.getText());
  }

  private FunctionExpression buildFromSpecialFunction(CommonTree token, CommonTree first) {
    return new FunctionExpression(token, "url", extractUrlParameter(token, first.getText()));
  }

  private Expression extractUrlParameter(CommonTree token, String text) {
    if (text==null)
      return null;
    
    if (text.length()<5)
      return new CssString(token, "");
    
    return new CssString(token, text.substring(4, text.length()-1));
  }

  //FIXME: write test for this, there is no such case
  private FunctionExpression buildFromNormalFunction(CommonTree token, CommonTree first) {
    String name = first.getText();
    Expression parameter = buildExpression(getChildren(token).get(1));
    return new FunctionExpression(token, name, parameter);
  }

  @SuppressWarnings("unchecked")
  private List<CommonTree> getChildren(CommonTree token) {
    return (List<CommonTree>) token.getChildren();
  }

}
