package com.github.sommeri.less4j.core.compiler.selectors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  
  private PerformedExtendsDB performedExtends = new PerformedExtendsDB();

  public void solveExtends(ASTCssNode node) {
    collectRulesets(node);
    solveInlineExtends();
  }

  private void solveInlineExtends() {
    for (Selector selector : inlineExtends) {
      solveInlineExtends(selector);
    }
  }

  private void solveInlineExtends(Selector extendingSelector) {
    for (RuleSet ruleSet : allRulesets) {
      List<Selector> selectors = new ArrayList<Selector>(ruleSet.getSelectors());
      for (Selector targetSelector : selectors) {
        Selector newSelector = constructNewSelector(extendingSelector, targetSelector);
        if (newSelector!=null && canExtend(extendingSelector, newSelector, ruleSet)) {
          doTheExtend(extendingSelector, newSelector, ruleSet, targetSelector);
        }
      }

    }
  }

  private void doTheExtend(Selector extendingSelector, Selector newSelector, RuleSet ruleSet, Selector targetSelector) {
    addSelector(ruleSet, newSelector);
    
    performedExtends.register(extendingSelector, targetSelector);

    Collection<Selector> thoseWhoExtendedExtending = performedExtends.getPreviousExtending(extendingSelector);
    for (Selector extendedExtending : thoseWhoExtendedExtending) {
      if (canExtend(extendedExtending, ruleSet)) {
        doTheExtend(extendedExtending, extendedExtending.clone(), ruleSet, targetSelector);
      }
    }
  }

  private boolean canExtend(Selector extendingSelector, RuleSet targetRuleSet) {
    return canExtend(extendingSelector, extendingSelector, targetRuleSet);
  }
  
  private boolean canExtend(Selector extendingSelector, Selector newSelector, RuleSet targetRuleSet) {
    if (containsSelector(newSelector, targetRuleSet))
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
      //if (comparator.contains(selector, extendingSelector))  
      if (comparator.equals(selector, extendingSelector))  
        return true;
    }
    return false;
  }

  private ASTCssNode findOwnerNode(ASTCssNode extendingSelector) {
    return manipulator.findParentOfType(extendingSelector, ASTCssNodeType.STYLE_SHEET, ASTCssNodeType.MEDIA);
  }

  private void addSelector(RuleSet ruleSet, Selector selector) {
    selector.setParent(ruleSet);
    ruleSet.addSelector(selector);
  }

  private Selector constructNewSelector(Selector extending, Selector possibleTarget) {
    if (possibleTarget == extending)
      return null;

    List<Extend> allExtends = extending.getExtend();
    for (Extend extend : allExtends) {
      if (!extend.isAll() && comparator.equals(possibleTarget, extend.getTarget())) 
        return extending.clone();
      
      if (extend.isAll()) {
        Selector addSelector = comparator.replaceInside(extend.getTarget(), possibleTarget, extend.getParentAsSelector());
        if (addSelector!=null)
          return addSelector;
      }
    }
    return null;
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
      if (selector.isExtending()) {
        inlineExtends.add(selector);
      }
    }
  }

}

class PerformedExtendsDB {
  
  private Map<Selector, List<Selector>> allSelectorExtends = new HashMap<Selector, List<Selector>>();
  
  protected List<Selector> getPreviousExtending(Selector selector) {
    List<Selector> result = allSelectorExtends.get(selector);
    if (result == null) {
      result = new ArrayList<Selector>();
      allSelectorExtends.put(selector, result);
    }

    return result;
  }

  protected void register(Selector extendingSelector, Selector targetSelector) {
    List<Selector> tied = getPreviousExtending(targetSelector);
    tied.add(extendingSelector);
  }

}