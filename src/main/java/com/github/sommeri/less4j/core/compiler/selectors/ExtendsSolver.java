package com.github.sommeri.less4j.core.compiler.selectors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.Extend;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.compiler.stages.ASTManipulator;

public class ExtendsSolver {

  private GeneralComparatorForExtend comparator = new GeneralComparatorForExtend();
  private ASTManipulator manipulator = new ASTManipulator();

  private List<RuleSet> allRulesets = new ArrayList<RuleSet>();
  private List<Selector> inlineExtends = new ArrayList<Selector>();

  private Map<Selector, List<Selector>> allSelectorExtends = new HashMap<Selector, List<Selector>>();

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

  private void solveInlineExtends(Selector extendingSelector) {
    AlreadyExtended alreadyExtended = new AlreadyExtended();
    alreadyExtended.markAsAlreadyExtended(extendingSelector, (RuleSet) extendingSelector.getParent());

    for (RuleSet ruleSet : allRulesets) {
      List<Selector> selectors = new ArrayList<Selector>(ruleSet.getSelectors());
      for (Selector targetSelector : selectors) {
        if (shouldExtend(extendingSelector, targetSelector) && canExtend(extendingSelector, alreadyExtended, ruleSet)) {
          doTheExtend(extendingSelector, alreadyExtended, ruleSet, targetSelector);

        }
      }

    }
  }

  private void doTheExtend(Selector extendingSelector, AlreadyExtended alreadyExtended, RuleSet ruleSet, Selector targetSelector) {
    performExtend(extendingSelector, ruleSet);
    addToThoseWhoExtended(extendingSelector, targetSelector);
    alreadyExtended.markAsAlreadyExtended(extendingSelector, ruleSet);

    List<Selector> thoseWhoExtendedExtending = new ArrayList<Selector>(getThoseWhoExtended(extendingSelector));
    for (Selector extendedExtending : thoseWhoExtendedExtending) {
      if (canExtend(extendedExtending, alreadyExtended, ruleSet)) {
        doTheExtend(extendedExtending, alreadyExtended, ruleSet, targetSelector);
      }
    }
  }

  private boolean canExtend(Selector extendingSelector, AlreadyExtended alreadyExtended, RuleSet targetRuleSet) {
    if (alreadyExtended.alreadyExtended(extendingSelector, targetRuleSet))
      return false;

    if (containsSelector(extendingSelector, targetRuleSet))
      return false;

    // selectors are able to extend only rulesets inside the same @media body.
    return compatibleMediaLocation(extendingSelector, targetRuleSet);
  }

  private boolean compatibleMediaLocation(Selector extendingSelector, RuleSet targetRuleSet) {
    ASTCssNode grandParent = findOwnerNode(extendingSelector);
    if (grandParent == null || grandParent.getType() == ASTCssNodeType.STYLE_SHEET)
      return true;

    return grandParent == findOwnerNode(targetRuleSet);
  }

  private boolean containsSelector(Selector extendingSelector, RuleSet targetRuleSet) {
    for (Selector selector : targetRuleSet.getSelectors()) {
      if (comparator.equals(selector, extendingSelector))
        return true;
    }
    return false;
  }

  private ASTCssNode findOwnerNode(ASTCssNode extendingSelector) {
    return manipulator.findParentOfType(extendingSelector, ASTCssNodeType.STYLE_SHEET, ASTCssNodeType.MEDIA);
  }

  private void performExtend(Selector extendingSelector, RuleSet ruleSet) {
    Selector selectorClone = extendingSelector.clone();
    selectorClone.setParent(ruleSet);
    ruleSet.addSelector(selectorClone);
  }

  private void addToThoseWhoExtended(Selector extendingSelector, Selector targetSelector) {
    List<Selector> tied = getThoseWhoExtended(targetSelector);
    tied.add(extendingSelector);
  }

  private List<Selector> getThoseWhoExtended(Selector selector) {
    List<Selector> result = allSelectorExtends.get(selector);
    if (result == null) {
      result = new ArrayList<Selector>();
      allSelectorExtends.put(selector, result);
    }

    return result;
  }

  private boolean shouldExtend(Selector extending, Selector possibleTarget) {
    if (possibleTarget == extending)
      return false;

    List<Extend> extendds = extending.getExtend();
    for (Extend extend : extendds) {
      if (comparator.equals(possibleTarget, extend.getTarget()))
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

class AlreadyExtended {

  private Map<Selector, Set<RuleSet>> alreadyExtended = new HashMap<Selector, Set<RuleSet>>();

  public void markAsAlreadyExtended(Selector selector, RuleSet ruleset) {
    Set<RuleSet> set = getSet(selector);
    set.add(ruleset);
  }

  public boolean alreadyExtended(Selector selector, RuleSet ruleSet) {
    Set<RuleSet> set = getSet(selector);
    return set.contains(ruleSet);
  }

  private Set<RuleSet> getSet(Selector selector) {
    Set<RuleSet> result = alreadyExtended.get(selector);
    if (result == null) {
      result = new HashSet<RuleSet>();
      alreadyExtended.put(selector, result);
    }

    return result;
  }
}
