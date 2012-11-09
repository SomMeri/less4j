package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

//FIXME: test special case: & & and &&
public class NestedSelectorAppender extends SelectorPart {

  private boolean directlyBefore;
  private boolean directlyAfter;

  public NestedSelectorAppender(HiddenTokenAwareTree underlyingStructure, boolean directlyBefore, boolean directlyAfter) {
    super(underlyingStructure);
    this.directlyBefore = directlyBefore;
    this.directlyAfter = directlyAfter;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

  public boolean isDirectlyBefore() {
    return directlyBefore;
  }

  public void setDirectlyBefore(boolean directlyBefore) {
    this.directlyBefore = directlyBefore;
  }

  public boolean isDirectlyAfter() {
    return directlyAfter;
  }

  public void setDirectlyAfter(boolean directlyAfter) {
    this.directlyAfter = directlyAfter;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.NESTED_SELECTOR_APPENDER;
  }

  @Override
  public NestedSelectorAppender clone() {
    return (NestedSelectorAppender)super.clone();
  }

  public boolean isAppender() {
    return true;
  }

  @Override
  public void setParent(ASTCssNode parent) {
    if (parent!=null && !(parent instanceof Selector))
      throw new IllegalArgumentException("Nested selector appender must belong to selector."); //TODO throw correct exception
    super.setParent(parent);
  }

  public Selector getParentAsSelector() {
    return (Selector) super.getParent();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("\"");
    if (!isDirectlyAfter())
      builder.append(" ");
    builder.append("&");
    if (!isDirectlyBefore())
      builder.append(" ");
    builder.append("\"");
    
    return builder.toString();
  }
  
  
}
