package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.compiler.CompileException;
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
    return hasSingleClassSelector();
  }

  public boolean isNamespace() {
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
  public PureMixin convertToMixin() {
    if (!isMixin()) //TODO throw warning here
      return null;
    
    SimpleSelector head = (SimpleSelector) selectors.get(0).getHead();
    CssClass className = (CssClass)head.getSubsequent().get(0);
    PureMixin pureMixin = new PureMixin(getUnderlyingStructure(), className.clone());
    pureMixin.setBody(getBody().clone());
    pureMixin.configureParentToAllChilds();
    return pureMixin;
  }

  /**
   * Behavior of this method is undefined if it is not a namespace.
   * @return 
   */
  public String extractNamespaceName() {
    if (!isNamespace())
      return "#error#"; //TODO throw some warning or whatever
    
    SimpleSelector selector = (SimpleSelector)selectors.get(0).getHead();
    if (hasSingleClassSelector()) {
      CssClass name = (CssClass)selector.getSubsequent().get(0);
      return name.getFullName();
    }
    if (hasSingleIdSelector()) {
      IdSelector name = (IdSelector)selector.getSubsequent().get(0);
      return name.getFullName();
    }
    
    //TODO throw special programming exception or something
    throw new CompileException("Not a namespace, can not extract the name.", this);
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
    //TODO: make old selectors forget the parent
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
