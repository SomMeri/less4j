package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

//FIXME: warning on viewport body with nested rulesets in output
public class ViewportBody extends Body<ASTCssNode> {

  public ViewportBody(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  public ViewportBody(HiddenTokenAwareTree underlyingStructure, List<ASTCssNode> members) {
    super(underlyingStructure, members);
  }

  public ViewportBody clone() {
    return (ViewportBody) super.clone();
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.VIEWPORT_BODY;
  }

}
