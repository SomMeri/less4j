package org.porting.less4j.core.ast;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

//FIXME: Named colors work only by accident, they do not even go to this class http://www.w3schools.com/cssref/css_colornames.asp
public class ColorExpression extends Expression {

  private String value;

  public ColorExpression(HiddenTokenAwareTree token, String value) {
    super(token);
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.COLOR_EXPRESSION;
  }

}
