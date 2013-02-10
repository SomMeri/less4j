package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class KeyframesBody extends Body {
  
  public KeyframesBody(HiddenTokenAwareTree token) {
    super(token);
  }

  public KeyframesBody(HiddenTokenAwareTree underlyingStructure, List<ASTCssNode> declarations) {
    super(underlyingStructure, declarations);
  }

  public List<ASTCssNode> getBody() {
    return super.getBody();
  }
  
  @Override
  public List<ASTCssNode> getChilds() {
    return super.getChilds();
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.KEYFRAMES_BODY;
  }

  @Override
  public KeyframesBody clone() {
    KeyframesBody result = (KeyframesBody) super.clone();
    result.configureParentToAllChilds();
    return result;
  }

}
