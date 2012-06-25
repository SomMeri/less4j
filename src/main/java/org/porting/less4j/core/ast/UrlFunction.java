package org.porting.less4j.core.ast;

import org.antlr.runtime.tree.CommonTree;

//FIXME: this is neither well tested nor finished. 
public class UrlFunction extends SpecialFunctionExpression {

  private String value;

  public UrlFunction(CommonTree token, String value) {
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
    return ASTCssNodeType.URL;
  }

}
