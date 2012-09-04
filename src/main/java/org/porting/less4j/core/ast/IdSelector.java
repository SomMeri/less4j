package org.porting.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

public class IdSelector extends ASTCssNode {

  private String name;

  public IdSelector(HiddenTokenAwareTree token, String name) {
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
  public List<? extends ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.ID_SELECTOR;
  }

}
