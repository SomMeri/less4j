package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public abstract class SelectorPart extends ASTCssNode {

  private SelectorCombinator leadingCombinator;

  public SelectorPart(HiddenTokenAwareTree underlyingStructure, SelectorCombinator leadingCombinator) {
    super(underlyingStructure);
    this.leadingCombinator = leadingCombinator;
  }

  public SelectorCombinator getLeadingCombinator() {
    return leadingCombinator;
  }

  public void setLeadingCombinator(SelectorCombinator leadingCombinator) {
    this.leadingCombinator = leadingCombinator;
  }

  public boolean hasLeadingCombinator() {
    return leadingCombinator!=null;
  }
  
  public boolean isClassesAndIdsOnlySelector() {
    return false;
  }

  public boolean isAppender() {
    return false;
  }

  public boolean isExtending() {
    return false;
  }

  @Override
  public List<ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList((ASTCssNode)leadingCombinator);
  }

  @Override
  public SelectorPart clone() {
    SelectorPart clone = (SelectorPart) super.clone();
    clone.setLeadingCombinator(getLeadingCombinator()==null? null : getLeadingCombinator().clone());
    return clone;
  }

}
