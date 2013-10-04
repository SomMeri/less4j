package com.github.sommeri.less4j.core.compiler.selectors;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Extend;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.Selector;

public class ExtendsSolver {

  private GeneralComparator comparator = new GeneralComparator();

  private List<RuleSet> allRulesets = new ArrayList<RuleSet>();
  private List<Selector> inlineExtends = new ArrayList<Selector>();

  public void solveExtends(ASTCssNode node) {
    collectRulesets(node);
    solveInlineExtends();
  }

  //FIXME: (!!!) test less.js for extends of extends
  private void solveInlineExtends() {
    for (Selector selector : inlineExtends) {
      solveInlineExtends(selector);
    }
  }

  private void solveInlineExtends(Selector selector) {
    for (RuleSet ruleSet : allRulesets) {
      List<Selector> selectors = new ArrayList<Selector>(ruleSet.getSelectors());
      for (Selector rulesetSelector : selectors) {
        if (shouldExtend(selector, rulesetSelector)) {
          Selector selectorClone = selector.clone();
          selectorClone.setParent(ruleSet);
          ruleSet.addSelector(selectorClone);
          //FIXME: (!!!) think about cyclic extening
        }
      }

    }
  }

  private boolean shouldExtend(Selector selector, Selector rulesetSelector) {
    if (rulesetSelector == selector)
      return false;

    List<Extend> extendds = selector.getExtend();
    for (Extend extend : extendds) {
      if (comparator.equals(rulesetSelector, extend.getTarget()))
        return true;
    }
    return false;
  }

  private void collectRulesets(ASTCssNode node) {
    switch (node.getType()) {
    case RULE_SET: {
      RuleSet ruleset = (RuleSet) node;
      allRulesets.add(ruleset);
      collectAllSelectors(ruleset);
      break;
    }
    default:
      List<? extends ASTCssNode> childs = new ArrayList<ASTCssNode>(node.getChilds());
      for (ASTCssNode kid : childs) {
        collectRulesets(kid);
      }
      break;
    }
  }

  private void collectAllSelectors(RuleSet ruleset) {
    for (Selector selector : ruleset.getSelectors()) {
      if (selector.isExtending())
        inlineExtends.add(selector);
    }
  }

}
