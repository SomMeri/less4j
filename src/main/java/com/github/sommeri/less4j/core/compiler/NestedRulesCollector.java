package com.github.sommeri.less4j.core.compiler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.Body;
import com.github.sommeri.less4j.core.ast.NestedRuleSet;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.Selector;

public class NestedRulesCollector {

  private Stack<List<Selector>> selectors;
  private LinkedList<NestedRuleSet> rulesets;

  public List<NestedRuleSet> collectNestedRuleSets(RuleSet kid) {
    selectors = new Stack<List<Selector>>();
    rulesets = new LinkedList<NestedRuleSet>();

    pushSelectors(kid);
    collectChildRuleSets(kid);
    popSelectors();

    return rulesets;
  }

  private void collectChildRuleSets(ASTCssNode node) {
    List<? extends ASTCssNode> childs = new ArrayList<ASTCssNode>(node.getChilds());
    for (ASTCssNode kid : childs) {
      if (kid.getType() == ASTCssNodeType.NESTED_RULESET) {
        NestedRuleSet nestedSet = (NestedRuleSet) kid;
        removeFromParent(nestedSet);
        collect(nestedSet);
        pushSelectors(nestedSet);
      }
      collectChildRuleSets(kid);
      if (kid.getType() == ASTCssNodeType.NESTED_RULESET) {
        popSelectors();
      }
    }
  }

  private void collect(NestedRuleSet nestedSet) {
    combine(nestedSet, selectors.peek(), nestedSet.isAppended());
    rulesets.add(nestedSet);
  }

  public void combine(RuleSet ruleSet, List<Selector> previousSelectors, boolean append) {
    List<Selector> innerSelectors = ruleSet.getSelectors();
    List<Selector> result = new ArrayList<Selector>();
    for (Selector inner : innerSelectors) {
      for (Selector outer : previousSelectors) {
        Selector outerClone = outer.clone();
        Selector innerClone = inner.clone();

        Selector rightestClone = outerClone.getRightestPart();
        if (append) {
          rightestClone.getHead().addSubsequent(innerClone.getHead().getSubsequent());
          rightestClone.getHead().configureParentToAllChilds();
        } else {
          rightestClone.setRight(innerClone);
          innerClone.setParent(rightestClone);
        }
        result.add(outerClone);
      }

    }
    ruleSet.replaceSelectors(result);
    ruleSet.configureParentToAllChilds();
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private void removeFromParent(NestedRuleSet nestedSet) {
    ASTCssNode parent = nestedSet.getParent();
    if (!(parent instanceof Body))
      throw new CompileException("Does not belong to body.", nestedSet);

    Body body = (Body) parent;
    body.removeMember(nestedSet);
    nestedSet.setParent(null);
  }

  private void pushSelectors(RuleSet kid) {
    selectors.push(new ArrayList<Selector>(kid.getSelectors()));
  }

  private void popSelectors() {
    selectors.pop();
  }

}
