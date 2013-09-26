package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class SimpleSelector extends SelectorPart implements Cloneable {

  private InterpolableName elementName;
  private boolean isStar;
  //*.warning and .warning are equivalent http://www.w3.org/TR/css3-selectors/#universal-selector
  //relevant only if the selector is a star
  private boolean isEmptyForm = false;
  private List<ElementSubsequent> subsequent = new ArrayList<ElementSubsequent>();
  private List<Extend> extend = new ArrayList<Extend>();

  public SimpleSelector(HiddenTokenAwareTree token, SelectorCombinator leadingCombinator, InterpolableName elementName, boolean isStar) {
    super(token, leadingCombinator);
    this.elementName = elementName;
    this.isStar = isStar;
  }

  @Override
  public boolean isClassesAndIdsOnlySelector() {
    return isLimitedPurposeSelector(ASTCssNodeType.CSS_CLASS, ASTCssNodeType.ID_SELECTOR);
  }

  private boolean isLimitedPurposeSelector(ASTCssNodeType... purposes) {
    if (elementName != null || !isEmptyForm() || subsequent == null)
      return false;
    
    Set<ASTCssNodeType> purposesSet = new HashSet<ASTCssNodeType>(Arrays.asList(purposes));
    for (ElementSubsequent es : subsequent) {
      if (!purposesSet.contains(es.getType()))
        return false;
      
      if (es.isInterpolated())
        return false;
    }
   
    return true;
  }

  public InterpolableName getElementName() {
    return elementName;
  }

  public boolean isStar() {
    return isStar;
  }

  public boolean isEmptyForm() {
    return isEmptyForm;
  }

  public void setEmptyForm(boolean isEmptyForm) {
    this.isEmptyForm = isEmptyForm;
  }

  public void setElementName(InterpolableName elementName) {
    this.elementName = elementName;
  }

  public void setStar(boolean isStar) {
    this.isStar = isStar;
  }

  public boolean hasElement() {
    return null != getElementName();
  }

  public List<ElementSubsequent> getSubsequent() {
    return subsequent;
  }

  public void addSubsequent(ElementSubsequent subsequent) {
    this.subsequent.add(subsequent);
  }

  public void addSubsequent(List<ElementSubsequent> subsequent) {
    this.subsequent.addAll(subsequent);
  }

  public List<Extend> getExtend() {
    return extend;
  }

  public void addExtend(Extend selector) {
    extend.add(selector);
  }

  @Override
  @NotAstProperty
  public List<ASTCssNode> getChilds() {
    List<ASTCssNode> result = super.getChilds();
    ArraysUtils.addIfNonNull(result, elementName);
    result.addAll(subsequent);
    result.addAll(extend);
    return result;
  }

  public ASTCssNodeType getType() {
    return ASTCssNodeType.SIMPLE_SELECTOR;
  }

  @Override
  public SimpleSelector clone() {
    SimpleSelector clone = (SimpleSelector) super.clone();
    clone.subsequent = ArraysUtils.deeplyClonedList(getSubsequent());
    clone.elementName = elementName==null? null : elementName.clone();
    clone.extend = ArraysUtils.deeplyClonedList(getExtend());
    clone.configureParentToAllChilds();
    return clone;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(isStar ? "*" : getElementName());
    builder.append(subsequent);
    return builder.toString();
  }

  public void extendName(String extension) {
    if (isStar) {
      isStar=false;
    }
    getElementName().extendName(extension);
  }

  public boolean isExtending() {
    return !extend.isEmpty();
  }

}
