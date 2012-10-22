package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class NestedRuleSet extends RuleSet {

  private boolean appended;
  private SelectorCombinator leadingCombinator;
  
  public NestedRuleSet(HiddenTokenAwareTree underlyingStructure, boolean appended, SelectorCombinator leadingCombinator, RuleSet ruleSet) {
    super(underlyingStructure);
    this.appended = appended;
    this.leadingCombinator = leadingCombinator;
    this.setBody(ruleSet.getBody());
    this.addSelectors(ruleSet.getSelectors());
  }

  public boolean isAppended() {
    return appended;
  }

  public void setAppended(boolean appended) {
    this.appended = appended;
  }

  public SelectorCombinator getLeadingCombinator() {
    return leadingCombinator;
  }

  public void setLeadingCombinator(SelectorCombinator leadingCombinator) {
    this.leadingCombinator = leadingCombinator;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    List<ASTCssNode> result = ArraysUtils.asNonNullList((ASTCssNode)leadingCombinator);
    result.addAll(super.getChilds());
    return result;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.NESTED_RULESET;
  } 
  
  public RuleSet convertToRuleSet() {
    return new RuleSet(getUnderlyingStructure(), getBody(), getSelectors());
  }

  @Override
  public NestedRuleSet clone() {
    NestedRuleSet result = (NestedRuleSet) super.clone();
    result.leadingCombinator = leadingCombinator==null?null:leadingCombinator.clone();
    result.configureParentToAllChilds();
    return result;
  }

}
