package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class SelectorOld extends ASTCssNode implements Cloneable {
  
  private SelectorPart head;
  private SelectorOld right;
  
  private List<SelectorPart> combinedParts = new ArrayList<SelectorPart>();

  public SelectorOld(HiddenTokenAwareTree token) {
    super(token);
  }
  
  public SelectorOld(HiddenTokenAwareTree token, SelectorPart head) {
    this(token, head, null);
  }

  public SelectorOld(HiddenTokenAwareTree token, SelectorPart head, SelectorOld right) {
    super(token);
    this.head = head;
    this.right = right;
  }
  
  public List<SelectorPart> getParts() {
    List<SelectorPart> parts = new ArrayList<SelectorPart>();
    parts.add(head);
    if (hasRight())
      parts.addAll(getRight().getParts());
    
    return parts;
  }

  /* *********************************************************** */
  //FIXME (!!!!!!-selector refactoring)
  public boolean isExtending() {
    if (hasRight())
      return getRightestPart().isExtending();

    return head.isExtending();
  }
  
  //FIXME (!!!- semi - selector refactoring)
  public SelectorPart getHead() {
    List<SelectorPart> parts = getParts();
    if (parts.isEmpty())
      return null;

    return parts.get(0);
  }

  //FIXME (!!!!!!-selector refactoring)
  public SelectorOld getRight() {
    return right;
  }

  //FIXME (!!!!!!-selector refactoring)
  public void setHead(SelectorPart head) {
    this.head = head;
  }

  //FIXME (!!!!!!-selector refactoring)
  public void setRight(SelectorOld right) {
    this.right = right;
  }

  //FIXME (!!!!!!-selector refactoring)
  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(head, right);
  }

  public ASTCssNodeType getType() {
    return ASTCssNodeType.SELECTOR;
  }

  //FIXME (!!!!!!-selector refactoring)
  @Override
  public SelectorOld clone() {
    SelectorOld clone = (SelectorOld) super.clone();
    clone.setHead(getHead().clone());
    clone.setRight(!hasRight()? null : getRight().clone());
    clone.configureParentToAllChilds();
    
    return clone;
  }

  //FIXME (!!!!!!-selector refactoring)
  public SelectorOld getRightestPart() {
    if (!hasRight())
      return this;
    
    return getRight().getRightestPart();
  }

  //FIXME (!!!!!!-selector refactoring)
  public boolean hasRight() {
    return getRight()!=null;
  }

  //FIXME (!!!!!!-selector refactoring)
  public boolean isCombined() {
    return getRight()!=null;
  }

  //FIXME (!!!!!!-selector refactoring)
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

  //FIXME (!!!!!!-selector refactoring)
  public NestedSelectorAppender findFirstAppender() {
    if (getHead().isAppender()) {
      return (NestedSelectorAppender) getHead();
    }

    if (!hasRight())
      return null;

    return getRight().findFirstAppender();
  }

  //FIXME (!!!!!!-selector refactoring)
  public boolean containsAppender() {
    return findFirstAppender() != null;
  }

  //FIXME (!!!!!!-selector refactoring)
  public boolean isReusableSelector() {
    if (getHead().isAppender())
      return isCombined() && getRight().isReusableSelector();
    
    SelectorOld current = this;
    while (current.isCombined()) {
      if (!current.hasReusableHead())
        return false;
      
      current = current.getRight();
    }
    
    return current.hasReusableHead();
  }

  //FIXME (!!!!!!-selector refactoring)
  private boolean hasReusableHead() {
    return getHead().isClassesAndIdsOnlySelector();
  }

  /**
   * Assumes that hasReusableHead returns true
   * @return
   */
  //FIXME (!!!!!!-selector refactoring)
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
    SelectorOld current = null;
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
