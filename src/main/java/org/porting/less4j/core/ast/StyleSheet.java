package org.porting.less4j.core.ast;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

public class StyleSheet extends ASTCssNode {

  public StyleSheet(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.STYLE_SHEET;
  }
}
