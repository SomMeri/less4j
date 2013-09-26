package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.problems.BugHappened;
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
    return leadingCombinator != null;
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
  @NotAstProperty
  public List<ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList((ASTCssNode) leadingCombinator);
  }

  @Override
  public SelectorPart clone() {
    SelectorPart clone = (SelectorPart) super.clone();
    clone.setLeadingCombinator(getLeadingCombinator() == null ? null : getLeadingCombinator().clone());
    return clone;
  }

  public List<ElementSubsequent> getSubsequent() {
    return new ArrayList<ElementSubsequent>();
  }

  public boolean hasElement() {
    return false;
  }

  public InterpolableName getElementName() {
    return null;
  }

  public ElementSubsequent getLastSubsequent() {
    List<ElementSubsequent> subsequent = getSubsequent();
    if (subsequent.isEmpty())
      return null;

    return subsequent.get(subsequent.size() - 1);
  }

  public boolean hasSubsequent() {
    return !getSubsequent().isEmpty();
  }


  public void addSubsequent(List<ElementSubsequent> subsequent) {
    throw new BugHappened("Attempt to add subsequent element to unexpected selector part.", this);
  }

  public void extendName(String secondName) {
    throw new BugHappened("Attempt to extend a name of unexpected selector part.", this);
  }

}
