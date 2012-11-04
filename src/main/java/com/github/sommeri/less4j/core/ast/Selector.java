package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class Selector extends ASTCssNode implements Cloneable {
  
  private NestedSelectorAppender beforeAppender;
  private NestedSelectorAppender afterAppender;

  private SelectorCombinator leadingCombinator;
  private SimpleSelector head;
  private Selector right;

  public Selector(HiddenTokenAwareTree token) {
    super(token);
  }
  
  public Selector(HiddenTokenAwareTree token, SelectorCombinator leadingCombinator, SimpleSelector head, SelectorCombinator combinator, Selector right) {
    super(token);
    this.leadingCombinator = leadingCombinator;
    this.head = head;
    this.right = right;
  }

  public NestedSelectorAppender getBeforeAppender() {
    return beforeAppender;
  }

  public void setBeforeAppender(NestedSelectorAppender beforeAppender) {
    this.beforeAppender = beforeAppender;
  }

  public NestedSelectorAppender getAfterAppender() {
    return afterAppender;
  }

  public void setAfterAppender(NestedSelectorAppender afterAppender) {
    this.afterAppender = afterAppender;
  }

  public boolean hasBeforeAppender() {
    return beforeAppender != null;
  }

  public boolean hasNoAppender() {
    return !hasBeforeAppender() && !hasAfterAppender();
  }

  public boolean hasBothAppenders() {
    return hasBeforeAppender() && hasAfterAppender();
  }

  public void addBeforeAppender(NestedSelectorAppender appender) {
    this.beforeAppender = appender;
  }

  public boolean hasAfterAppender() {
    return afterAppender!=null;
  }

  public void addAfterAppender(NestedSelectorAppender appender) {
    this.afterAppender = appender;
  }

  public boolean hasLeadingCombinator() {
    return leadingCombinator!=null;
  }
  
  public SelectorCombinator getLeadingCombinator() {
    return leadingCombinator;
  }

  public void setLeadingCombinator(SelectorCombinator leadingCombinator) {
    this.leadingCombinator = leadingCombinator;
  }

  public SimpleSelector getHead() {
    return head;
  }

  public Selector getRight() {
    return right;
  }

  public void setHead(SimpleSelector head) {
    this.head = head;
  }

  public void setRight(Selector right) {
    this.right = right;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(beforeAppender, (ASTCssNode)leadingCombinator, afterAppender, head, right);
  }

  public ASTCssNodeType getType() {
    return ASTCssNodeType.SELECTOR;
  }

  @Override
  public Selector clone() {
    Selector clone = (Selector) super.clone();
    clone.addBeforeAppender(beforeAppender==null? null : beforeAppender.clone());
    clone.addAfterAppender(afterAppender==null? null : afterAppender.clone());
    clone.setLeadingCombinator(getLeadingCombinator()==null? null : getLeadingCombinator().clone());
    clone.setHead(getHead().clone());
    clone.setRight(!hasRight()? null : getRight().clone());
    clone.configureParentToAllChilds();
    
    return clone;
  }

  public Selector getRightestPart() {
    if (!hasRight())
      return this;
    
    return getRight().getRightestPart();
  }

  public boolean hasRight() {
    return getRight()!=null;
  }

  public boolean isCombined() {
    return getRight()!=null;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Selector [");
    builder.append(leadingCombinator);
    builder.append(head);
    builder.append(" ");
    builder.append(right);
    builder.append("]");
    return builder.toString();
  }

}
