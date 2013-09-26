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
import com.github.sommeri.less4j.utils.ArraysUtils;

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

    return indirectJoinNoClone(first, combinator, second);
  }

  public Selector indirectJoinNoClone(Selector first, SelectorCombinator combinator, Selector second) {
    return indirectJoinNoClone(first, combinator, second.getParts());
  }

  public Selector indirectJoinNoClone(Selector first, SelectorCombinator combinator, List<SelectorPart> second) {
    if (second.isEmpty())
      return first;
    
    if (combinator != null) {
      second.get(0).setLeadingCombinator(combinator);
    }
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
    List<SelectorPart> secondParts = ArraysUtils.deeplyClonedList(secondI.getParts());
    SelectorPart secondHead = secondParts.get(0);

    if (secondHead.isAppender())
      return indirectJoinNoClone(first, secondHead.getLeadingCombinator(), secondParts);
    
    /*
     * FIXME: test on old whether survives if first is not simple selector. (say, if:
     * (~"escaped") {
     *   &:pseudo() {
     *   }
     * }
     * 
     */
    SelectorPart attachToHead = first.getLastPart();
    directlyJoinParts(attachToHead, secondHead);

    secondParts.remove(0);
    if (!secondParts.isEmpty()) {
      return indirectJoinNoClone(first, secondParts.get(0).getLeadingCombinator(), secondParts);
    }

    return first;
  }

  private void directlyJoinParts(SelectorPart first, SelectorPart second) {
    if (second.hasElement()) {
      String secondName = second.hasElement() ? second.getElementName().getName() : "";
      if (first.hasSubsequent()) {
        ElementSubsequent subsequent = first.getLastSubsequent();
        subsequent.extendName(secondName);
      } else {
        first.extendName(secondName);
      }
    }

    first.addSubsequent(second.getSubsequent());
    first.configureParentToAllChilds();
  }

  private Collection<Selector> replaceFirstAppender(Selector selector, List<Selector> previousSelectors) {
    if (selector.getHead().isAppender()) {
      NestedSelectorAppender appender = (NestedSelectorAppender) selector.getHead();
      return joinAll(previousSelectors, chopOffHead(selector), appender.getLeadingCombinator(), appender.isDirectlyBefore());
    }

    // appender somewhere in the middle
    NestedSelectorAppender appender = selector.findFirstAppender();
    if (appender == null)
      throw new BugHappened("This is very weird error and should not happen.", selector);

    Selector  afterAppender = splitOn(selector, appender);
    List<Selector> partialResults = joinAll(selector, previousSelectors, appender.getLeadingCombinator(), appender.isDirectlyAfter());
    return joinAll(partialResults, afterAppender, null, appender.isDirectlyBefore());
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
    if (!selector.isCombined())
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

  private Selector splitOn(Selector selector, NestedSelectorAppender appender) {
    List<SelectorPart> parts = selector.getParts();
    int indexOfAppender = parts.indexOf(appender);
    List<SelectorPart> appenderAndAfter = parts.subList(indexOfAppender, parts.size());
    
    //remove appender
    appenderAndAfter.remove(0);
    appender.setParent(null);

    //create selector with after appender parts
    Selector result = null;
    if (!appenderAndAfter.isEmpty()) {
      result = new Selector(selector.getUnderlyingStructure(), new ArrayList<SelectorPart>(appenderAndAfter));
      result.configureParentToAllChilds();
    }
    
    //leave only before appender parts in original selector
    appenderAndAfter.clear();
    return result;
  }

}
