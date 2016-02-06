package com.github.sommeri.less4j.core.compiler.selectors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNode.Visibility;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.Extend;
import com.github.sommeri.less4j.core.ast.MultiTargetExtend;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.compiler.stages.ASTManipulator;
import com.github.sommeri.less4j.utils.ArraysUtils;
import com.github.sommeri.less4j.utils.Couple;

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
    // performance optimization - create the array only when it is needed
    // this came out of profiling and is worth keeping this way
    ArrayList<ExtendRefs> doExtends = null;
    for (RuleSet ruleSet : allRulesets) {
      for (Selector targetSelector : ruleSet.getSelectors()) {
        Selector newSelector = constructNewSelector(extendingSelector, targetSelector);
        if (newSelector != null && canExtend(extendingSelector, newSelector, ruleSet)) {
          if (doExtends == null) {
            doExtends = new ArrayList<ExtendRefs>();
          }
          doExtends.add(new ExtendRefs(ruleSet, targetSelector, newSelector));
        }
      }
    }
    if (doExtends != null) {
      for (ExtendRefs refs : doExtends) {
        doTheExtend(extendingSelector, refs.newSelector, refs.ruleSet, refs.targetSelector);
      }
    }
  }

   private void doTheExtend(Selector originalExtendingSelector, Selector addSelector, RuleSet target, Selector targetSelector) {
    addSelector(target, addSelector);

    performedExtends.register(targetSelector, originalExtendingSelector, addSelector.getVisibility());

    // Following solves this situation: 
    // ** extendingSelector: b
    // ** targetSelector: a
    //
    // a {}
    // c:extends(b) {}
    // b:extends(a) {}
    //
    // Since c extended b before. Therefore, when b is extending a, c needs to extend it too.
    //
    Collection<PerformedExtend> mayNeedToExtendTargetToo = performedExtends.getThoseWhoExtended(originalExtendingSelector);
    for (PerformedExtend info : mayNeedToExtendTargetToo) {
      Selector selector = info.getSelector();
      if (canExtend(selector, target)) {
        Selector nextSelector = selector.clone();
        manipulator.setTreeVisibility(nextSelector, info.getVisibility());
        doTheExtend(selector, nextSelector, target, targetSelector);
      }
    }
  }

  private boolean canExtend(Selector extendingSelector, RuleSet targetRuleSet) {
    return canExtend(extendingSelector, extendingSelector, targetRuleSet);
  }

  private boolean canExtend(Selector extendingSelector, Selector newSelector, RuleSet targetRuleSet) {
    if (containsSelector(newSelector, targetRuleSet)) {
      return false;
    }

    // selectors are able to extend only rulesets inside the same @media body.
    return compatibleMediaLocation(extendingSelector, targetRuleSet);
  }

  private boolean compatibleMediaLocation(Selector extendingSelector, RuleSet targetRuleSet) {
    ASTCssNode grandParent = findOwnerNode(extendingSelector);
    if (grandParent == null || grandParent.getType() == ASTCssNodeType.STYLE_SHEET)
      return true;

    boolean result = grandParent == findOwnerNode(targetRuleSet);
    return result;
  }

  private boolean containsSelector(Selector extendingSelector, RuleSet targetRuleSet) {
    for (Selector selector : targetRuleSet.getSelectors()) {
      // if (comparator.contains(selector, extendingSelector))
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

    if (selector.getVisibility() == Visibility.VISIBLE) {
      //visibility of children
      manipulator.setTreeVisibility(ruleSet.getBody(), Visibility.VISIBLE);
      ruleSet.setVisibility(Visibility.VISIBLE);

      //visibility of parents
      ASTCssNode node = ruleSet;
      while (node.hasParent()) {
        node = node.getParent();
        switch (node.getVisibility()) {
        case DEFAULT:
          node.setVisibility(Visibility.VISIBLE);
          break;
        case VISIBLE:
          break;
        }
      }
      
    }

  }

  private Selector constructNewSelector(Selector extending, Selector possibleTarget) {
    if (possibleTarget == extending)
      return null;

    List<Extend> allExtends = extending.getExtend();
    for (Extend extend : allExtends) {
      if (!extend.isAll() && comparator.equals(possibleTarget, extend.getTarget()))
        return setNewSelectorVisibility(extend, extending.clone());

      if (extend.isAll()) {
        Selector addSelector = comparator.replaceInside(extend.getTarget(), possibleTarget, extend.getParentAsSelector());
        if (addSelector != null)
          return setNewSelectorVisibility(extend, addSelector);
      }
    }
    return null;
  }

  private Selector setNewSelectorVisibility(Extend extend, Selector newSelector) {
    manipulator.setTreeVisibility(newSelector, extend.getVisibility());

    return newSelector;
  }

  private void collectRulesets(ASTCssNode node) {
    switch (node.getType()) {
    case RULE_SET: {
      RuleSet ruleset = (RuleSet) node;
      allRulesets.add(ruleset);
      collectExtendingSelectors(ruleset);
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

  private void collectExtendingSelectors(RuleSet ruleset) {
    List<Extend> directExtends = collectDirectExtendDeclarations(ruleset);
    for (Selector selector : ruleset.getSelectors()) {
      addClones(selector, directExtends);
      if (selector.isExtending()) {
        inlineExtends.add(selector);
      }
    }
  }

  private void addClones(Selector selector, List<Extend> newExtends) {
    List<Extend> clones = ArraysUtils.deeplyClonedList(newExtends);
    selector.addExtends(clones);
    for (Extend extend : clones) {
      extend.setParent(selector);
    }
  }

  private List<Extend> collectDirectExtendDeclarations(RuleSet ruleset) {
    List<Extend> result = new ArrayList<Extend>();
    List<ASTCssNode> members = new ArrayList<ASTCssNode>(ruleset.getBody().getMembers());
    for (ASTCssNode node : members) {
      if (node.getType() == ASTCssNodeType.EXTEND) {
        Extend extend = (Extend) node;
        manipulator.removeFromBody(extend);
        result.add(extend);
      } else if (node.getType() == ASTCssNodeType.MULTI_TARGET_EXTEND) {
        MultiTargetExtend extend = (MultiTargetExtend) node;
        manipulator.removeFromBody(extend);
        result.addAll(extend.getAllExtends());
      }
    }
    return result;
  }

  private static class ExtendRefs {
    ExtendRefs(RuleSet ruleSet, Selector targetSelector, Selector newSelector) {
      this.ruleSet = ruleSet;
      this.targetSelector = targetSelector;
      this.newSelector = newSelector;
    }

    RuleSet ruleSet;
    Selector targetSelector;
    Selector newSelector;
    
    @Override
    public String toString() {
      String visibility = "[" + ruleSet.getVisibility() + ", " + targetSelector.getVisibility() + ", "+newSelector.getVisibility() + "]";
      String string = "ExtendRefs "+visibility+"[\n  ruleSet=" + ruleSet + ",\n"
          + "  targetSelector=" + targetSelector + ",\n"
              + "  newSelector=" + newSelector + "\n]";
      return string;
    }
    
  }

  private static class PerformedExtendsDB {

    private Map<Selector, List<PerformedExtend>> allSelectorExtends = new HashMap<Selector, List<PerformedExtend>>();

    protected List<PerformedExtend> getThoseWhoExtended(Selector selector) {
      List<PerformedExtend> result = allSelectorExtends.get(selector);
      if (result == null) {
        result = new ArrayList<PerformedExtend>();
        allSelectorExtends.put(selector, result);
      }

      return result;
    }

    protected void register(Selector targetSelector, Selector extendingSelector, Visibility extendingSelectorVisibility) {
      List<PerformedExtend> tied = getThoseWhoExtended(targetSelector);
      tied.add(new PerformedExtend(extendingSelector, extendingSelectorVisibility));
    }

  }

  private static class PerformedExtend extends Couple<Selector, Visibility> {

    public PerformedExtend(Selector extendingSelector, Visibility extendingSelectorVisibility) {
      super(extendingSelector, extendingSelectorVisibility);
    }
    
    public Selector getSelector() {
      return getT();
    }
    public Visibility getVisibility() {
      return getM();
    }
  }
  
}


