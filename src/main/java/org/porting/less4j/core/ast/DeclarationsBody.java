package org.porting.less4j.core.ast;

import java.util.List;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

//FIXME: page is not done
public class DeclarationsBody extends Body<Declaration> {

  public DeclarationsBody(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  public DeclarationsBody(HiddenTokenAwareTree underlyingStructure, List<Declaration> declarations) {
    super(underlyingStructure, declarations);
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.DECLARATIONS_BODY;
  }

}
