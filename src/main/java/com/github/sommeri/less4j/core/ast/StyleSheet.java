package com.github.sommeri.less4j.core.ast;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class StyleSheet extends Body<ASTCssNode> {
  
  public StyleSheet(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.STYLE_SHEET;
  }

}
