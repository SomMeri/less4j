package org.porting.less4j.core.ast;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

//TODO: use this to add error handling to it
public class Unknown extends ASTCssNode {

  public Unknown(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  public ASTCssNodeType getType() {
    return ASTCssNodeType.UNKNOWN;
  }
}
