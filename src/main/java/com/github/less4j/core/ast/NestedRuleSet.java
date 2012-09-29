package com.github.less4j.core.ast;

import java.util.List;

import com.github.less4j.core.parser.HiddenTokenAwareTree;
import com.github.less4j.utils.ArraysUtils;

public class NestedRuleSet extends ASTCssNode {

  private boolean appended;
  private SelectorCombinator leadingCombinator;
  private RuleSet ruleSet;
  
  public NestedRuleSet(HiddenTokenAwareTree underlyingStructure, boolean appended, SelectorCombinator leadingCombinator, RuleSet ruleSet) {
    super(underlyingStructure);
    this.appended = appended;
    this.leadingCombinator = leadingCombinator;
    this.ruleSet = ruleSet; 
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

  public RuleSet getRuleSet() {
    return ruleSet;
  }

  public void setRuleSet(RuleSet ruleSet) {
    this.ruleSet = ruleSet;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(leadingCombinator, ruleSet);
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.NESTED_RULESET;
  } 

}
