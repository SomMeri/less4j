package org.porting.less4j.core.ast;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

public class MediumType extends ASTCssNode {

  private String name;

  public MediumType(HiddenTokenAwareTree underlyingStructure, String name) {
    super(underlyingStructure);
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
    return ASTCssNodeType.MEDIUM_TYPE;
  }

}
