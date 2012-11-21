package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.problems.BugHappened;
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

  public boolean isReusableStructure() {
    return hasSingleClassSelector() || hasSingleIdSelector();
  }

  private boolean hasSingleClassSelector() {
    if (!hasSimpleSelecor())
      return false;
    
    SelectorPart selector = selectors.get(0).getHead();
    return selector.isSingleClassSelector();
  }

  private boolean hasSingleIdSelector() {
    if (!hasSimpleSelecor())
      return false;
    
    SelectorPart selector = selectors.get(0).getHead();
    return selector.isSingleIdSelector();
  }

  private boolean hasSimpleSelecor() {
    if (selectors==null||selectors.size()!=1)
      return false;
    
    Selector first = selectors.get(0);
    if (first.isCombined())
      return false;
    
    return true;
  }

  /**
   * Behavior of this method is undefined if it is not mixin.
   * @return 
   */
  public ReusableStructure convertToReusableStructure() {
    if (!isReusableStructure()) 
      throw new BugHappened("Caller is supposed to check for this.", this);
    
    SimpleSelector head = (SimpleSelector) selectors.get(0).getHead();
    ElementSubsequent className = (ElementSubsequent)head.getSubsequent().get(0);
    ReusableStructure reusable = new ReusableStructure(getUnderlyingStructure(), className.clone());
    reusable.setBody(getBody().clone());
    reusable.configureParentToAllChilds();
    return reusable;
  }

  /**
   * Behavior of this method is undefined if it is not a namespace.
   * @return 
   */
  public String extractReusableStructureName() {
    if (!isReusableStructure())
      throw new BugHappened("Caller is supposed to check for this.", this);
    
    SimpleSelector selector = (SimpleSelector)selectors.get(0).getHead();
    ElementSubsequent name = selector.getSubsequent().get(0);
    return name.getFullName();
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
