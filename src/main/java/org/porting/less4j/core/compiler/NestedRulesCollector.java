package org.porting.less4j.core.compiler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.porting.less4j.core.ast.ASTCssNode;
import org.porting.less4j.core.ast.ASTCssNodeType;
import org.porting.less4j.core.ast.Body;
import org.porting.less4j.core.ast.NestedRuleSet;
import org.porting.less4j.core.ast.RuleSet;
import org.porting.less4j.core.ast.Selector;

//TODO: test appender (and other similar) in combination with multiple selectors asdf, asdf, asdf { & inner } 
public class NestedRulesCollector {

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
    RuleSet ruleSet = nestedSet.getRuleSet();
    combine(ruleSet, selectors.peek(), nestedSet.isAppended());
    rulesets.add(ruleSet);
  }

  public void combine(RuleSet ruleSet, List<Selector> previousSelectors, boolean append) {
    //FIXME: we need to set parents correctly, and copy old selectors so one node does not belong to two parents
    List<Selector> innerSelectors = ruleSet.getSelectors();
    List<Selector> result = new ArrayList<Selector>();
    for (Selector inner : innerSelectors) {
      for (Selector outer : previousSelectors) {
        Selector outerClone = outer.clone();
        Selector innerClone = inner.clone();

        Selector rightestClone = outerClone.getRightestPart();
        if (append)
          rightestClone.getHead().addSubsequent(innerClone.getHead().getSubsequent());
        else 
          rightestClone.setRight(innerClone);
        result.add(outerClone);
      }

    }
    ruleSet.replaceSelectors(result);
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

  private void pushSelectors(NestedRuleSet nestedSet) {
    pushSelectors(nestedSet.getRuleSet());
  }

  private void popSelectors() {
    selectors.pop();
  }

}
