package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class NestedSelectorAppender extends ASTCssNode {

  private boolean isDirect;
  
  public NestedSelectorAppender(HiddenTokenAwareTree underlyingStructure, boolean isDirect) {
    super(underlyingStructure);
    this.isDirect = isDirect;
  }

  public boolean isDirect() {
    return isDirect;
  }

  public void setDirect(boolean isDirect) {
    this.isDirect = isDirect;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.NESTED_SELECTOR_APPENDER;
  }

  @Override
  public NestedSelectorAppender clone() {
    return (NestedSelectorAppender)super.clone();
  }

}
