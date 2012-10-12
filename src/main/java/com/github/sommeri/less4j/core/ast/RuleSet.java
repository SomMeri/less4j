package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class RuleSet extends ASTCssNode {

  private List<Selector> selectors = new ArrayList<Selector>();
  private RuleSetsBody body;

  public RuleSet(HiddenTokenAwareTree token) {
    super(token);
  }

  public RuleSet(HiddenTokenAwareTree token, RuleSetsBody body, List<Selector> selectors) {
    super(token);
    this.body = body;
    addSelectors(selectors);
    configureParentToAllChilds();
  }

  public List<Selector> getSelectors() {
   return selectors;
  }
  
  public RuleSetsBody getBody() {
    return body;
  }

  public boolean hasEmptyBody() {
    return body==null? true : body.isEmpty();
  }

  public void setBody(RuleSetsBody body) {
    this.body = body;
  }

  public boolean isMixin() {
    if (selectors==null||selectors.size()!=1)
      return false;
    
    Selector first = selectors.get(0);
    if (first.isCombined())
      return false;
    
    SimpleSelector selector = first.getHead();
    return selector.isSingleClassSelector();
  }
  
  /**
   * Behavior of this method is undefined if it is not mixin.
   * @return 
   */
  public PureMixin convertToMixin() {
    CssClass className = (CssClass)selectors.get(0).getHead().getSubsequent().get(0);
    PureMixin pureMixin = new PureMixin(getUnderlyingStructure(), className.clone());
    pureMixin.setBody(getBody().clone());
    pureMixin.configureParentToAllChilds();
    return pureMixin;
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

  public void replaceSelectors(List<Selector> result) {
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
