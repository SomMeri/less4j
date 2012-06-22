package org.porting.less4j.core.ast;

import org.antlr.runtime.tree.CommonTree;

public class StyleSheet extends ASTCssNode {

  public StyleSheet(CommonTree underlyingStructure) {
    super(underlyingStructure);
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.STYLE_SHEET;
  }
}
