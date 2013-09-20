package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class Selector extends ASTCssNode implements Cloneable {
  
  private SelectorPart head;
  private Selector right;

  public Selector(HiddenTokenAwareTree token) {
    super(token);
  }
  
  public Selector(HiddenTokenAwareTree token, SelectorPart head) {
    this(token, head, null);
  }

  public Selector(HiddenTokenAwareTree token, SelectorPart head, Selector right) {
    super(token);
    this.head = head;
    this.right = right;
  }

  public boolean isExtending() {
    if (hasRight())
      return getRightestPart().isExtending();

    return head.isExtending();
  }
  
  public SelectorPart getHead() {
    return head;
  }

  public Selector getRight() {
    return right;
  }

  public void setHead(SelectorPart head) {
    this.head = head;
  }

  public void setRight(Selector right) {
    this.right = right;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(head, right);
  }

  public ASTCssNodeType getType() {
    return ASTCssNodeType.SELECTOR;
  }

  @Override
  public Selector clone() {
    Selector clone = (Selector) super.clone();
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
    builder.append(head);
    builder.append(" ");
    builder.append(right);
    builder.append("]");
    return builder.toString();
  }

  public NestedSelectorAppender findFirstAppender() {
    if (getHead().isAppender()) {
      return (NestedSelectorAppender) getHead();
    }

    if (!hasRight())
      return null;

    return getRight().findFirstAppender();
  }

  public boolean containsAppender() {
    return findFirstAppender() != null;
  }

  public boolean isReusableSelector() {
    if (getHead().isAppender())
      return isCombined() && getRight().isReusableSelector();
    
    Selector current = this;
    while (current.isCombined()) {
      if (!current.hasReusableHead())
        return false;
      
      current = current.getRight();
    }
    
    return current.hasReusableHead();
  }

  private boolean hasReusableHead() {
    return getHead().isClassesAndIdsOnlySelector();
  }

  /**
   * Assumes that hasReusableHead returns true
   * @return
   */
  public ReusableStructureName toReusableStructureName() {
    List<ElementSubsequent> nameParts = extractReusableNameParts();
    
    ReusableStructureName result = new ReusableStructureName(nameParts.get(0).getUnderlyingStructure(), nameParts);
    return result;
  }

  // We are loosing a lot of information during the extraction. It is ok, 
  // because less.js is not using combinators and does not distinguish 
  // between .aaa.bbb and .aaa .bbb
  private List<ElementSubsequent> extractReusableNameParts() {
    List<ElementSubsequent> result = new ArrayList<ElementSubsequent>();
    Selector current = null;
    do {
      //initialize or move to next
      current = current==null? this : current.getRight();
      //extract name parts
      SelectorPart currentHead = current.getHead();
      if (!currentHead.isAppender()) {
        result.addAll(((SimpleSelector) currentHead).getSubsequent());
      }
    } while (current.isCombined());
    
    return result;
  }

  //FIXME: (!!!!!) remove
  @Deprecated
  public void setLeadingCombinator(SelectorCombinator combinator) {
    head.setLeadingCombinator(combinator);
  }

  //FIXME: (!!!!!) remove
  @Deprecated
  public SelectorCombinator getLeadingCombinator() {
    return head.getLeadingCombinator();
  }

  //FIXME: (!!!!!) remove
  @Deprecated
  public boolean hasLeadingCombinator() {
    return head.hasLeadingCombinator();
  }
}
