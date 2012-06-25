package org.porting.less4j.core.parser;

import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.porting.less4j.core.ast.ColorExpression;
import org.porting.less4j.core.ast.CssString;
import org.porting.less4j.core.ast.Expression;
import org.porting.less4j.core.ast.IdentifierExpression;
import org.porting.less4j.core.ast.NumberExpression;
import org.porting.less4j.core.ast.NumberExpression.Sign;
import org.porting.less4j.core.ast.SpecialFunctionExpression;
import org.porting.less4j.core.ast.UrlFunction;

public class ExpressionBuilder {

  private final ASTBuilderSwitch parentSwitch;

  public ExpressionBuilder(ASTBuilderSwitch parentSwitch) {
    super();
    this.parentSwitch = parentSwitch;
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

      //TODO    | a+=function_or_identifier
    //TODO    | a+=special_function


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

  private SpecialFunctionExpression buildFromSpecialFunction(CommonTree token, CommonTree first) {
    return new UrlFunction(token, first.getText());
  }

  @SuppressWarnings("unchecked")
  private List<CommonTree> getChildren(CommonTree token) {
    return (List<CommonTree>) token.getChildren();
  }

}
