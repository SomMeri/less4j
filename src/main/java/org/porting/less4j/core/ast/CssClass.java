package org.porting.less4j.core.ast;

import org.antlr.runtime.tree.CommonTree;

public class CssClass extends ASTCssNode {

  private String name;

  public CssClass(CommonTree token, String name) {
    //TODO asserts
    super(token);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.CSS_CLASS;
  }

}
