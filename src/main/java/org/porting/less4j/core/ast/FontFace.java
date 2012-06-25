package org.porting.less4j.core.ast;

import java.util.List;

import org.antlr.runtime.tree.CommonTree;

//FIXME: not done yet
public class FontFace extends ASTCssNode {

  public FontFace(CommonTree token) {
    super(token);
  }

  @Override
  public List<ASTCssNode> getChilds() {
    return super.getChilds();
  }


  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.FONT_FACE;
  }

}
