package org.porting.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.tree.CommonTree;

public class FontFace extends ASTCssNode {

  private List<Declaration> body = new ArrayList<Declaration>();

  public FontFace(CommonTree token) {
    super(token);
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return body;
  }

  public List<Declaration> getDeclarations() {
    return body;
  }

  public boolean hasEmptyBody() {
    return body.isEmpty();
  }

  public void addDeclarations(List<Declaration> body) {
    this.body.addAll(body);
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.FONT_FACE;
  }

}
