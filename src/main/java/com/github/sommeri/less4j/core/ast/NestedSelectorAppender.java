package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.problems.BugHappened;

public class NestedSelectorAppender extends SelectorPart {

  public NestedSelectorAppender(HiddenTokenAwareTree underlyingStructure, SelectorCombinator leadingCombinator) {
    super(underlyingStructure, leadingCombinator);
  }

  @Override
  @NotAstProperty
  public List<ASTCssNode> getChilds() {
    return super.getChilds();
  }

  public boolean isDirectlyAfter() {
    return !hasLeadingCombinator();
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
      throw new BugHappened("Nested selector appender must belong to selector.", this); 
    super.setParent(parent);
  }

  public Selector getParentAsSelector() {
    return (Selector) super.getParent();
  }

  //FIXME (now) proper toString
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("\"");
    if (!isDirectlyAfter())
      builder.append(" ");
    builder.append("&");
    builder.append("\"");
    
    return builder.toString();
  }
  
  
}
