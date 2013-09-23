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

  //FIXME (!!!) should these be here? it certainly helps to avoid casts
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
    // FIXME  (!!!)  implement for real
    return null;
  }

  public void addSubsequent(List<ElementSubsequent> subsequent) {
    //FIXME: try to force this and if it is possible solve correctly
    throw new BugHappened("Attempt to add subsequent element to wrong selector part.", this);
  }

  public boolean hasSubsequent() {
    // FIXME  (!!!)  implement for real
    return false;
  }

  public void extendName(String secondName) {
    //FIXME: try to force this and if it is possible solve correctly
    throw new BugHappened("Attempt to extend a name of wrong selector part.", this);
  }

}
