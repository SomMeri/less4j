package org.porting.less4j.core.ast;

import org.antlr.runtime.tree.CommonTree;

public class CssString extends Expression {

  private String value;

  public CssString(CommonTree token, String value) {
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
    return ASTCssNodeType.STRING_EXPRESSION;
  }

}
