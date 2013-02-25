package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.problems.BugHappened;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class RuleSet extends ASTCssNode implements BodyOwner<GeneralBody> {

  private List<Selector> selectors = new ArrayList<Selector>();
  private GeneralBody body;

  public RuleSet(HiddenTokenAwareTree token) {
    super(token);
  }

  public RuleSet(HiddenTokenAwareTree token, GeneralBody body, List<Selector> selectors) {
    super(token);
    this.body = body;
    addSelectors(selectors);
    configureParentToAllChilds();
  }

  public List<Selector> getSelectors() {
   return selectors;
  }
  
  public GeneralBody getBody() {
    return body;
  }

  public boolean hasEmptyBody() {
    return body==null? true : body.isEmpty();
  }

  public void setBody(GeneralBody body) {
    this.body = body;
  }

  public boolean usableAsReusableStructure() {
    for (Selector selector : selectors) {
      if (selector.isReusableSelector())
        return true;
    }
    return false;
  }

  /**
   * Behavior of this method is undefined if it is not mixin.
   * @return 
   */
  public ReusableStructure convertToReusableStructure() {
    if (!usableAsReusableStructure()) 
      throw new BugHappened("Caller is supposed to check for this.", this);
    
    List<ReusableStructureName> reusableNames = new ArrayList<ReusableStructureName>();
    for (Selector selector : selectors) if (selector.isReusableSelector()) {
      SimpleSelector reusableSelector = selector.asReusableSelector();
      reusableNames.add(new ReusableStructureName(reusableSelector.getUnderlyingStructure(), reusableSelector.getSubsequent()));
    }
    
    ReusableStructure reusable = new ReusableStructure(getUnderlyingStructure(), reusableNames);
    reusable.setBody(getBody().clone());
    reusable.configureParentToAllChilds();
    return reusable;
  }

  /**
   * Behavior of this method is undefined if it is not a namespace.
   * @return 
   */
  public List<String> extractReusableStructureNames() {
    List<String> result = new ArrayList<String>();
    for (Selector selector : selectors) {
      if (selector.isReusableSelector()) {
        SimpleSelector reusableSelector = selector.asReusableSelector();
        result.add(reusableSelector.getSubsequent().get(0).getFullName());
      }
    }

    if (result.isEmpty()) {
        throw new BugHappened("Not convertible to resusable structure - caller was supposed to check for this.", this);
    }
    
    return result;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    List<ASTCssNode> result = ArraysUtils.asNonNullList((ASTCssNode)body);
    result.addAll(0, selectors);
    return result;
  }

  public void addSelectors(List<Selector> selectors) {
    this.selectors.addAll(selectors);
  }

  public void addSelector(Selector selector) {
    this.selectors.add(selector);
  }

  public void replaceSelector(Selector oldSelector, Selector newSelector) {
    oldSelector.setParent(null);
    newSelector.setParent(this);
    
    Collections.replaceAll(this.selectors, oldSelector, newSelector);
  }

  public void replaceSelectors(List<Selector> result) {
    for (Selector oldSelector : selectors) {
      oldSelector.setParent(null);
    }
    selectors = new ArrayList<Selector>();
    selectors.addAll(result);
  }

  @Override
  public RuleSet clone() {
    RuleSet result = (RuleSet) super.clone();
    result.body = body==null?null:body.clone();
    result.selectors = ArraysUtils.deeplyClonedList(selectors);
    result.configureParentToAllChilds();
    return result;
  }

  public ASTCssNodeType getType() {
    return ASTCssNodeType.RULE_SET;
  }

}
