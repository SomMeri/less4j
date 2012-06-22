package org.porting.less4j.core.ast;

import org.antlr.runtime.tree.CommonTree;

public class Unknown extends ASTCssNode {

  public Unknown(CommonTree underlyingStructure) {
    super(underlyingStructure);
  }

  public ASTCssNodeType getType() {
    return ASTCssNodeType.UNKNOWN;
  }
}
