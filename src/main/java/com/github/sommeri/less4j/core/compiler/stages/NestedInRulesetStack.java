package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Media;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.compiler.selectors.SelectorsManipulator;

public class NestedInRulesetStack {
  private final SelectorsManipulator selectorsManipulator = new SelectorsManipulator();

  private Stack<List<Selector>> selectors = new Stack<List<Selector>>();
  private LinkedList<ASTCssNode> nestedNodes = new LinkedList<ASTCssNode>();

  public NestedInRulesetStack(RuleSet topLevelNode) {
    pushSelectors(topLevelNode);
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

  public void collect(Media media) {
    nestedNodes.add(media);
  }

  private void combine(RuleSet ruleSet, List<Selector> previousSelectors) {
    List<Selector> result = new ArrayList<Selector>();
    for (Selector selector : ruleSet.getSelectors()) {
      result.addAll(selectorsManipulator.replaceAppenders(selector, previousSelectors));
    }

    ruleSet.replaceSelectors(result);
    ruleSet.configureParentToAllChilds();
  }

  public List<Selector> currentSelectors() {
    return selectors.peek();
  }

}
