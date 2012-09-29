package com.github.less4j.core.ast;

import java.util.List;

import com.github.less4j.core.parser.HiddenTokenAwareTree;
import com.github.less4j.utils.ArraysUtils;

public class Selector extends ASTCssNode implements Cloneable {
  
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
    return ArraysUtils.asNonNullList((ASTCssNode)leadingCombinator, head, right);
  }

  public ASTCssNodeType getType() {
    return ASTCssNodeType.SELECTOR;
  }

  //FIXME: unit tests na clonning!!!!
  //FIXME: are the parents really needed? They seem only to complicate things right now
  @Override
  public Selector clone() {
    Selector clone = (Selector) super.clone();
    clone.setLeadingCombinator(getLeadingCombinator()==null? null : getLeadingCombinator().clone());
    clone.setHead(getHead().clone());
    clone.setRight(getRight()==null? null : getRight().clone());
    
    return clone;
  }

  public Selector getRightestPart() {
    if (getRight()==null)
      return this;
    
    return getRight().getRightestPart();
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
