package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Directive;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.compiler.selectors.SelectorsManipulator;

public class NestedInRulesetStack {
  private final SelectorsManipulator selectorsManipulator = new SelectorsManipulator();

  private Stack<List<Selector>> selectors = new Stack<List<Selector>>();
  private LinkedList<ASTCssNode> nestedNodes = new LinkedList<ASTCssNode>();

  public NestedInRulesetStack(RuleSet topLevelNode) {
    List<Selector> topSelectors = new ArrayList<Selector>();
    for (Selector selector : topLevelNode.getSelectors()) {
      topSelectors.add(selectorsManipulator.removeAppenders(selector.clone()));
    }
    selectors.push(topSelectors);
  }

  public void popSelectors() {
    selectors.pop();
  }

  public void pushSelectors(RuleSet kid) {
    selectors.push(new ArrayList<Selector>(kid.getSelectors()));
  }

  public List<ASTCssNode> getRulesets() {
    return nestedNodes;
  }

  public void collect(RuleSet nestedSet) {
    combine(nestedSet, selectors.peek());
    nestedNodes.add(nestedSet);
  }

  public void collect(Directive directive) {
    nestedNodes.add(directive);
  }

  private void combine(RuleSet ruleSet, List<Selector> previousSelectors) {
    List<Selector> result = new ArrayList<Selector>();
    for (Selector selector : ruleSet.getSelectors()) {
      // FIXME: meri: review whether they have all the right visibility in the
      // end
      result.addAll(selectorsManipulator.replaceAppenders(selector, previousSelectors));
    }

    ruleSet.replaceSelectors(result);
    ruleSet.configureParentToAllChilds();
  }

  public List<Selector> currentSelectors() {
    return selectors.peek();
  }

}
