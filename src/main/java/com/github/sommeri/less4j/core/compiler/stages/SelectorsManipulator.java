package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ElementSubsequent;
import com.github.sommeri.less4j.core.ast.NestedSelectorAppender;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.ast.SelectorCombinator;
import com.github.sommeri.less4j.core.ast.SelectorPart;
import com.github.sommeri.less4j.core.problems.BugHappened;

public class SelectorsManipulator {

  public List<Selector> replaceAppenders(Selector inSelector, List<Selector> replacements) {
    if (!inSelector.containsAppender()) {
      return indirectJoinAll(replacements, null, inSelector);
    }

    List<Selector> result = Arrays.asList(inSelector);
    while (result.get(0).containsAppender()) {
      List<Selector> nextRound = new ArrayList<Selector>();
      for (Selector tbch : result) {
        nextRound.addAll(replaceFirstAppender(tbch, replacements));
      }

      result = nextRound;
    }

    return result;
  }

  private List<Selector> indirectJoinAll(List<Selector> first, SelectorCombinator combinator, Selector second) {
    List<Selector> result = new ArrayList<Selector>();
    for (Selector previous : first) {
      result.add(indirectJoin(previous, combinator, second));
    }

    return result;
  }

  public Selector indirectJoin(Selector firstI, SelectorCombinator combinator, Selector secondI) {
    // if both of them are null, something is very wrong
    if (secondI == null)
      return firstI.clone();

    if (firstI == null)
      return secondI.clone();

    Selector first = firstI.clone();
    Selector second = secondI.clone();

    indirectJoinNoClone(first, combinator, second);

    return first;
  }

  public Selector indirectJoinNoClone(Selector first, SelectorCombinator combinator, Selector second) {
    //FIXME: (!) what with extend? 
    if (combinator != null)
      second.setLeadingCombinator(combinator);
    
    first.addParts(second.getParts());
    first.configureParentToAllChilds();

    return first;
  }

  public Selector indirectJoinNoClone(Selector first, SelectorCombinator combinator, List<SelectorPart> second) {
    //FIXME: (!) what with extend? 
    if (combinator != null)
      second.get(0).setLeadingCombinator(combinator);
    
    first.addParts(second);
    first.configureParentToAllChilds();

    return first;
  }

  public Selector directJoin(Selector firstI, Selector secondI) {
    // if both of them are null, something is very wrong
    if (secondI == null)
      return firstI.clone();

    if (firstI == null)
      return secondI.clone();

    Selector first = firstI.clone();
    Selector second = secondI.clone();

    if (second.getHead().isAppender())
      return indirectJoinNoClone(first, second.getLeadingCombinator(), second);
    
    /*
     * FIXME: test on old whether sufvives if first is not simple selector. (say, if:
     * (~"escaped") {
     *   &:pseudo() {
     *   }
     * }
     * 
     */
    SelectorPart attachToHead = first.findLastPart();
    SelectorPart secondHead = second.getHead();
    if (!secondHead.hasElement()) {
      attachToHead.addSubsequent(secondHead.getSubsequent());
    } else {
      String secondName = secondHead.hasElement() ? secondHead.getElementName().getName() : "";
      if (attachToHead.hasSubsequent()) {
        ElementSubsequent subsequent = attachToHead.getLastSubsequent();
        subsequent.extendName(secondName);
      } else {
        attachToHead.extendName(secondName);
      }
      attachToHead.addSubsequent(secondHead.getSubsequent());
    }

    attachToHead.configureParentToAllChilds();

    if (second.hasRight()) {
      second.getAllExceptHead();
      indirectJoinNoClone(first, second.findSecondPart().getLeadingCombinator(), second.getAllExceptHead());
    }

    return first;
  }

  private Collection<Selector> replaceFirstAppender(Selector selector, List<Selector> previousSelectors) {
    if (selector.getHead().isAppender()) {
      NestedSelectorAppender appender = (NestedSelectorAppender) selector.getHead();
      return joinAll(previousSelectors, chopOffHead(selector), selector.getLeadingCombinator(), appender.isDirectlyBefore());
    }

    // appender somewhere in the middle
    NestedSelectorAppender appender = selector.findFirstAppender();
    if (appender == null)
      throw new BugHappened("This is very weird error and should not happen.", selector);

    Selector  afterAppender = splitOn(selector, appender);
    
    List<Selector> partialResults = joinAll(selector, previousSelectors, appender.getLeadingCombinator(), appender.isDirectlyAfter());
    return joinAll(partialResults, afterAppender, null, appender.isDirectlyBefore());
    
    //FIXME (!!) when it is all done, remove following. Also refactor this class.
//    Selector rightSelectorBeginning = appender.getParentAsSelector();
//    Selector leftSelectorBeginning = splitOn(rightSelectorBeginning);
//    List<Selector> partialResults = joinAll(leftSelectorBeginning, previousSelectors, rightSelectorBeginning.getLeadingCombinator(), appender.isDirectlyAfter());
//    return joinAll(partialResults, chopOffHead(rightSelectorBeginning), null, appender.isDirectlyBefore());
  }

  private List<Selector> joinAll(Selector first, List<Selector> seconds, SelectorCombinator leadingCombinator, boolean appenderDirectlyPlaced) {
    //pretending null as after appender, less.js does not handle this case anyway
    //this case being:
    //.input-group-addon + {
    //  .selector {
    //    heeej: hoou;
    //  }
    //}
    boolean directJoin = isDirect(leadingCombinator, appenderDirectlyPlaced, null); 
    if (directJoin)
      return directJoinAll(first, seconds);
    else
      return indirectJoinAll(leadingCombinator, first, seconds);
  }

  private List<Selector> joinAll(List<Selector> firsts, Selector second, SelectorCombinator beforeAppenderCombinator, boolean appenderDirectlyPlaced) {
    boolean directJoin = isDirect(beforeAppenderCombinator, appenderDirectlyPlaced, second);
    if (directJoin)
      return directJoinAll(firsts, second);
    else
      return indirectJoinAll(firsts, beforeAppenderCombinator, second);
  }

  private Selector chopOffHead(Selector selector) {
    if (!selector.hasRight())
      return null;
    
    selector.removeHead();
    return selector;
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
      result.add(indirectJoin(first, leadingCombinator, second));
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

  private boolean isDirect(SelectorCombinator beforeAppenderCombinator, boolean appenderDirectlyPlaced, Selector afterAppender) {
    return beforeAppenderCombinator == null && appenderDirectlyPlaced && (afterAppender == null || !afterAppender.hasLeadingCombinator());
  }

//  private Selector splitOn(Selector rightSelectorStart) {
//    Selector leftSelectorEnding = (Selector) rightSelectorStart.getParent();
//    leftSelectorEnding.setRight(null);
//    rightSelectorStart.setParent(null);
//    Selector leftSelectorBeginning = leftSelectorEnding;
//    while (leftSelectorBeginning.getParent() instanceof Selector) {
//      leftSelectorBeginning = (Selector) leftSelectorBeginning.getParent();
//    }
//
//    return leftSelectorBeginning;
//  }

  private Selector splitOn(Selector selector, NestedSelectorAppender appender) {
    //FIXME (!!!) do not manipulate selectors list from outside
    List<SelectorPart> parts = selector.getParts();
    int indexOfAppender = parts.indexOf(appender);
    List<SelectorPart> appenderAndAfter = parts.subList(indexOfAppender, parts.size());
    appenderAndAfter.remove(0);
    //FIXME (!!!) iny underlying and refactor whole class so it does not create middle selecotrs
    Selector result = null;
    if (!appenderAndAfter.isEmpty())
      result = new Selector(selector.getUnderlyingStructure(), new ArrayList<SelectorPart>(appenderAndAfter));
    
    appenderAndAfter.clear();
    
    appender.setParent(null);
    if (result!=null)
      result.configureParentToAllChilds();
    selector.configureParentToAllChilds();
    
    return result;
  }

}
