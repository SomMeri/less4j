package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class Selector extends ASTCssNode implements Cloneable {

  private List<SelectorPart> combinedParts = new ArrayList<SelectorPart>();
  private List<Extend> extend = new ArrayList<Extend>();

  public Selector(HiddenTokenAwareTree token) {
    super(token);
  }

  public Selector(HiddenTokenAwareTree token, SelectorPart head) {
    this(token, ArraysUtils.asModifiableList(head));
  }

  public Selector(HiddenTokenAwareTree token, List<SelectorPart> combinedParts) {
    super(token);
    this.combinedParts = combinedParts;
  }

  public List<SelectorPart> getParts() {
    return combinedParts;
  }

  public void addPart(SelectorPart part) {
    combinedParts.add(part);
  }

  public void addParts(List<SelectorPart> parts) {
    combinedParts.addAll(parts);
  }

  public void removeHead() {
    combinedParts.remove(0);
  }

  public boolean isExtending() {
    return !extend.isEmpty();
  }

  public List<Extend> getExtend() {
    return extend;
  }

  public void setExtend(List<Extend> extend) {
    this.extend = extend;
  }

  @NotAstProperty
  public SelectorPart getHead() {
    List<SelectorPart> parts = getParts();
    if (parts.isEmpty())
      return null;

    return parts.get(0);
  }

  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    ArrayList<ASTCssNode> result = new ArrayList<ASTCssNode>(combinedParts);
    result.addAll(extend);
    return result;
  }

  public ASTCssNodeType getType() {
    return ASTCssNodeType.SELECTOR;
  }

  @Override
  public Selector clone() {
    Selector clone = (Selector) super.clone();
    clone.combinedParts = ArraysUtils.deeplyClonedList(combinedParts);
    clone.extend = ArraysUtils.deeplyClonedList(extend);
    clone.configureParentToAllChilds();

    return clone;
  }

  @NotAstProperty
  public SelectorPart getLastPart() {
    return ArraysUtils.last(getParts());
  }

  public boolean isCombined() {
    return combinedParts.size() > 1;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Selector [");
    builder.append(combinedParts);
    builder.append("]");
    return builder.toString();
  }

  public NestedSelectorAppender findFirstAppender() {
    for (SelectorPart part : getParts()) {
      if (part.isAppender())
        return (NestedSelectorAppender) part;
    }
    
    return null;
  }

  public boolean containsAppender() {
    return findFirstAppender() != null;
  }

  public boolean isReusableSelector() {
    Iterator<SelectorPart> parts = getParts().iterator();
    if (!parts.hasNext())
      return false;
    
    // skip initial appenders
    SelectorPart current = parts.next();
    while (current.isAppender() && parts.hasNext()) {
      current = parts.next();
    }

    // find out whether there is something not reusable 
    while (current.isClassesAndIdsOnlySelector() && parts.hasNext()) {
      current = parts.next();
    }

    return current.isClassesAndIdsOnlySelector();
  }

  /**
   * Assumes that hasReusableHead returns true
   * 
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
    for (SelectorPart part : getParts()) {
      if (!part.isAppender()) {
        result.addAll(((SimpleSelector) part).getSubsequent());
      }
    }
    return result;
  }

  public boolean hasLeadingCombinator() {
    if (getHead()==null)
      return false;
    
    return getHead().hasLeadingCombinator();
  }

  public boolean isEmpty() {
    return getParts().isEmpty();
  }

  public void addExtend(Extend extend) {
    this.extend.add(extend);
  }

}
