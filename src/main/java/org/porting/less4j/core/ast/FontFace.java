package org.porting.less4j.core.ast;

import java.util.List;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

public class FontFace extends Body<Declaration> {

  public FontFace(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  public FontFace(HiddenTokenAwareTree underlyingStructure, List<Declaration> declarations) {
    super(underlyingStructure, declarations);
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.FONT_FACE;
  }

}
