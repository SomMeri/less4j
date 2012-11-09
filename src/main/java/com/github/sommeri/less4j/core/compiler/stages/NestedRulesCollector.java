package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.github.sommeri.less4j.core.ast.SelectorCombinator;
import com.github.sommeri.less4j.core.ast.SimpleSelector;
import com.github.sommeri.less4j.core.compiler.CompileException;
import com.github.sommeri.less4j.utils.ArraysUtils;

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
    List<Selector> result = new ArrayList<Selector>();
    for (Selector selector : ruleSet.getSelectors()) {
      result.addAll(combine(selector, previousSelectors));
    }

    ruleSet.replaceSelectors(result);
    ruleSet.configureParentToAllChilds();
  }

  private Collection<Selector> combine(Selector selector, List<Selector> previousSelectors) {
    if (!containsAppender(selector)) {
      return indirectJoinAll(null, previousSelectors, selector);
    }

    //TODO: optimize a bit
    List<Selector> result = Arrays.asList(selector);
    while (containsAppender(result.get(0))) {
      List<Selector> nextRound = new ArrayList<Selector>();
      for (Selector tbch : result) {
        nextRound.addAll(replaceFirstAppender(tbch, previousSelectors));
      }

      result = nextRound;
    }

    return result;
  }

  private List<Selector> indirectJoinAll(SelectorCombinator appenderCombinator, List<Selector> first, Selector second) {
    List<Selector> result = new ArrayList<Selector>();
    for (Selector previous : first) {
      result.add(indirectJoin(appenderCombinator, previous, second));
    }

    return result;
  }

  private List<Selector> directJoinAll(List<Selector> firsts, Selector second) {
    List<Selector> result = new ArrayList<Selector>();
    for (Selector first : firsts) {
      result.add(directJoin(first, second));
    }

    return result;
  }

  private List<Selector> indirectJoinAll(SelectorCombinator leadingCombinator, Selector first, List<Selector> seconds) {
    List<Selector> result = new ArrayList<Selector>();
    for (Selector second : seconds) {
      result.add(indirectJoin(leadingCombinator, first, second));
    }

    return result;
  }

  private List<Selector> directJoinAll(Selector first, List<Selector> seconds) {
    List<Selector> result = new ArrayList<Selector>();
    for (Selector second : seconds) {
      result.add(directJoin(first, second));
    }

    return result;
  }

  private boolean containsAppender(Selector selector) {
    return findFirstAppender(selector)!=null;
  }

  private Collection<Selector> replaceFirstAppender(Selector selector, List<Selector> previousSelectors) {
    if (selector.getHead().isAppender()) {
      return replaceHeadAppender(selector, previousSelectors);
    }

    //appender somewhere in the middle
    NestedSelectorAppender appender = findFirstAppender(selector);
    if (appender == null)
      throw new IllegalStateException("This is very weird error and should not happen.");  //TODO throw correct exception
    
    Selector rightSelectorBeginning = appender.getParentAsSelector();
    Selector leftSelectorBeginning = splitOn(rightSelectorBeginning);
    List<Selector> partialResults = joinAll(leftSelectorBeginning, previousSelectors, rightSelectorBeginning.getLeadingCombinator(), appender.isDirectlyAfter());
    return joinAll(partialResults, chopOffHead(rightSelectorBeginning), null, appender.isDirectlyBefore());
  }

  private List<Selector> joinAll(Selector first, List<Selector> seconds, SelectorCombinator leadingCombinator, boolean appenderDirectlyPlaced) {
    boolean directJoin = isDirect(leadingCombinator, appenderDirectlyPlaced);
    if (directJoin )
      return directJoinAll(first, seconds);
    else return indirectJoinAll(leadingCombinator, first, seconds);
  }

  private List<Selector> joinAll(List<Selector> firsts, Selector second, SelectorCombinator leadingCombinator, boolean appenderDirectlyPlaced) {
    boolean directJoin = isDirect(leadingCombinator, appenderDirectlyPlaced);
    if (directJoin )
      return directJoinAll(firsts, second);
    else return indirectJoinAll(leadingCombinator, firsts, second);
  }

  private boolean isDirect(SelectorCombinator leadingCombinator, boolean appenderDirectlyPlaced) {
    return leadingCombinator==null && appenderDirectlyPlaced;
  }

  private Selector splitOn(Selector rightSelectorStart) {
    Selector leftSelectorEnding = (Selector)rightSelectorStart.getParent();
    leftSelectorEnding.setRight(null);
    rightSelectorStart.setParent(null);
    Selector leftSelectorBeginning = leftSelectorEnding;
    while (leftSelectorBeginning.getParent() instanceof Selector) {
      leftSelectorBeginning = (Selector)leftSelectorBeginning.getParent();
    }
    
    return leftSelectorBeginning;
  }

  private Collection<Selector> replaceHeadAppender(Selector selector, List<Selector> previousSelectors) {
    NestedSelectorAppender appender = (NestedSelectorAppender) selector.getHead();
    Selector right = chopOffHead(selector);
    
    if (appender.isDirectlyBefore() && !selector.hasLeadingCombinator()) {
      return directJoinAll(previousSelectors, right);
    } else {
      if (right!=null) {
        return indirectJoinAll(selector.getLeadingCombinator(), previousSelectors, right);
      } else {
        return overrideCombinator(selector.getLeadingCombinator(), ArraysUtils.deeplyClonedList(previousSelectors));
      }
    }
  }

  private Collection<Selector> overrideCombinator(SelectorCombinator leadingCombinator, List<Selector> selectors) {
    for (Selector selector : selectors) {
      selector.setLeadingCombinator(leadingCombinator);
    }
    return selectors;
  }

  private Selector chopOffHead(Selector selector) {
    if (!selector.hasRight())
      return null;
    
    Selector right = selector.getRight();
    right.setParent(null);
    selector.setRight(null);
    return right;
  }

  private NestedSelectorAppender findFirstAppender(Selector selector) {
    if (selector.getHead().isAppender()) {
      return (NestedSelectorAppender)selector.getHead();
    }

    if (!selector.hasRight())
      return null;

    return findFirstAppender(selector.getRight());
  }

    private Selector directJoin(Selector firstI, Selector secondI) {
      //if both of them are null, something is very wrong
      if (secondI==null)
        return firstI.clone();

      if (firstI==null)
        return secondI.clone();
      
      Selector first = firstI.clone();
      Selector second = secondI.clone();
  
      if (second.getHead().isAppender())
        return indirectJoinNoClone(second.getLeadingCombinator(), first, second);
      
      Selector attachTo = first.getRightestPart();
      SimpleSelector attachToHead = (SimpleSelector)attachTo.getHead();
      SimpleSelector secondHead = (SimpleSelector)second.getHead();
      if (!secondHead.hasElement()) {
        attachToHead.addSubsequent(secondHead.getSubsequent());
      } else {
        String secondName = secondHead.hasElement() ? secondHead.getElementName() : "";
        if (attachToHead.hasSubsequent()) {
          ElementSubsequent subsequent = attachToHead.getLastSubsequent();
          subsequent.extendName(secondName);
        } else {
          attachToHead.extendName(secondName);
        }
        attachToHead.addSubsequent(secondHead.getSubsequent());
      }
  
      attachToHead.configureParentToAllChilds();
  
      if (second.hasRight())
        indirectJoinNoClone(second.getLeadingCombinator(), attachTo, second.getRight());
  
      return first;
    }

  private Selector indirectJoin(SelectorCombinator appenderCombinator, Selector firstI, Selector secondI) {
    //if both of them are null, something is very wrong
    if (secondI==null)
      return firstI.clone();

    if (firstI==null)
      return secondI.clone();

    Selector first = firstI.clone();
    Selector second = secondI.clone();

    indirectJoinNoClone(appenderCombinator, first, second);

    return first;
  }

  private Selector indirectJoinNoClone(SelectorCombinator appenderCombinator, Selector first, Selector second) {
    Selector attachTo = first.getRightestPart();
    attachTo.setRight(second);
    second.setParent(attachTo);
    if (appenderCombinator!=null)
      second.setLeadingCombinator(appenderCombinator);
    
    return first;
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
