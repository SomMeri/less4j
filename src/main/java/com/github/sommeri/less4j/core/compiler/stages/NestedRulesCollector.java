package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.Selector;

public class NestedRulesCollector {

  private final ASTManipulator manipulator = new ASTManipulator();
  private final SelectorsManipulator selectorsManipulator = new SelectorsManipulator();
  private Stack<List<Selector>> selectors;
  private LinkedList<RuleSet> rulesets;

  public List<RuleSet> collectNestedRuleSets(RuleSet kid) {
    selectors = new Stack<List<Selector>>();
    rulesets = new LinkedList<RuleSet>();

    pushSelectors(kid);
    collectChildRuleSets(kid);
    popSelectors();

    return rulesets;
  }

  private void collectChildRuleSets(ASTCssNode node) {
    List<? extends ASTCssNode> childs = new ArrayList<ASTCssNode>(node.getChilds());
    for (ASTCssNode kid : childs) {
      if (kid.getType() == ASTCssNodeType.RULE_SET) {
        RuleSet nestedSet = (RuleSet) kid;
        manipulator.removeFromBody(nestedSet);
        collect(nestedSet);
        pushSelectors(nestedSet);
      }
      collectChildRuleSets(kid);
      if (kid.getType() == ASTCssNodeType.RULE_SET) {
        popSelectors();
      }
    }
  }

  private void collect(RuleSet nestedSet) {
    combine(nestedSet, selectors.peek());
    rulesets.add(nestedSet);
  }

  private void combine(RuleSet ruleSet, List<Selector> previousSelectors) {
    List<Selector> result = new ArrayList<Selector>();
    for (Selector selector : ruleSet.getSelectors()) {
      result.addAll(selectorsManipulator.replaceAppenders(selector, previousSelectors));
    }

    ruleSet.replaceSelectors(result);
    ruleSet.configureParentToAllChilds();
  }

  private void popSelectors() {
    selectors.pop();
  }

  private void pushSelectors(RuleSet kid) {
    selectors.push(new ArrayList<Selector>(kid.getSelectors()));
  }

}
