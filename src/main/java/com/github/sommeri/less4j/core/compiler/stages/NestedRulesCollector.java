package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.Body;
import com.github.sommeri.less4j.core.ast.ElementSubsequent;
import com.github.sommeri.less4j.core.ast.NestedSelectorAppender;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.ast.SimpleSelector;
import com.github.sommeri.less4j.core.compiler.CompileException;

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
      if (kid.getType() == ASTCssNodeType.RULE_SET) {
        RuleSet nestedSet = (RuleSet) kid;
        removeFromParent(nestedSet);
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

  public void combine(RuleSet ruleSet, List<Selector> previousSelectors) {
    List<Selector> innerSelectors = ruleSet.getSelectors();
    List<Selector> result = new ArrayList<Selector>();
    for (Selector inner : innerSelectors) {
      for (Selector outer : previousSelectors) {
        if (inner.hasNoAppender()) {
          result.add(indirectJoin(outer, inner));
        } else if (inner.hasBothAppenders()) {
          result.addAll(join(outer, inner, previousSelectors));
        } else if (inner.hasBeforeAppender()) {
          result.add(join(outer, inner, inner.getBeforeAppender()));
        } else if (inner.hasAfterAppender()) {
          result.add(join(inner, outer, inner.getAfterAppender()));
        }
      }

    }
    ruleSet.replaceSelectors(result);
    ruleSet.configureParentToAllChilds();
  }

  private Collection<Selector> join(Selector outer, Selector inner, List<Selector> previousSelectors) {
    Collection<Selector> result = new ArrayList<Selector>();
    Selector base = join(outer, inner, inner.getBeforeAppender());
    for (Selector selector : previousSelectors) {
      result.add(join(base, selector, inner.getAfterAppender()));
    }
    return result;
  }

  private Selector join(Selector first, Selector second, NestedSelectorAppender appender) {
    if (appender.isDirect()) {
      return directJoin(first, second);
    } else {
      return indirectJoin(first, second);
    }
  }

  private Selector directJoin(Selector firstI, Selector secondI) {
    Selector first = firstI.clone();
    Selector second = secondI.clone();
    
    Selector attachTo = first.getRightestPart();
    SimpleSelector attachToHead = attachTo.getHead();
    if (!second.getHead().hasElement()) {
      attachToHead.addSubsequent(second.getHead().getSubsequent());
    } else {
      SimpleSelector secondHead = second.getHead();
      String secondName = secondHead.hasElement()?secondHead.getElementName() : "";
      if (attachToHead.hasSubsequent()) {
        ElementSubsequent subsequent=attachToHead.getLastSubsequent();
        subsequent.extendName(secondName);
      } else {
        attachToHead.extendName(secondName);
        attachToHead.addSubsequent(secondHead.getSubsequent());
      }
      System.out.println(attachToHead.getElementName());
    }

    attachToHead.configureParentToAllChilds();

    if (second.hasRight())
      indirectJoinNoClone(attachTo, second.getRight());
    
    return first;
  }

  private Selector indirectJoin(Selector firstI, Selector secondI) {
    Selector first = firstI.clone();
    Selector second = secondI.clone();
    
    indirectJoinNoClone(first, second);
    
    return first;
  }

  private void indirectJoinNoClone(Selector first, Selector second) {
    Selector attachTo = first.getRightestPart();
    attachTo.setRight(second);
    second.setParent(attachTo);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private void removeFromParent(RuleSet nestedSet) {
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
