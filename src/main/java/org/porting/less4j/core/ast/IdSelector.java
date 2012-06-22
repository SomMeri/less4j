package org.porting.less4j.core.ast;

import org.antlr.runtime.tree.CommonTree;

public class IdSelector extends ASTCssNode {

  private String name;

  public IdSelector(CommonTree token, String name) {
    super(token);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.ID_SELECTOR;
  }

}
