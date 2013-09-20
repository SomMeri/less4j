package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.Selector;

public class ExtendsSolver {
  
  private List<RuleSet> allRulesets = new ArrayList<RuleSet>();
  private List<Selector> inlineExtends = new ArrayList<Selector>();

  public void solveExtends(ASTCssNode node) {
    collectRulesets(node);
    solveInlineExtends();
  }

  //FIXME: (!!!) test less.js for extends of extends
  private void solveInlineExtends() {
    for (RuleSet ruleSet : allRulesets) {
      solveInlineExtends(ruleSet);
    }
  }

  private void solveInlineExtends(RuleSet ruleSet) {
    for (Selector selector : ruleSet.getSelectors()) {
      if (selector.isExtending()) {
        System.out.println(selector);
      }
    }
    // TODO Auto-generated method stub
    
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
