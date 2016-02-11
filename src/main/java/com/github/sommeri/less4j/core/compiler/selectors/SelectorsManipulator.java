package com.github.sommeri.less4j.core.compiler.selectors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ElementSubsequent;
import com.github.sommeri.less4j.core.ast.Extend;
import com.github.sommeri.less4j.core.ast.NestedSelectorAppender;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.ast.SelectorCombinator;
import com.github.sommeri.less4j.core.ast.SelectorPart;
import com.github.sommeri.less4j.core.ast.SimpleSelector;
import com.github.sommeri.less4j.core.problems.BugHappened;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class SelectorsManipulator {

  public Selector removeAppenders(Selector selector) {
    selector = replaceLeadingAppendersByEmptiness(selector);
    if (!selector.containsAppender())
      return selector;
    
    Selector replacement = replaceMiddleAppendersByEmptiness(selector);
    return replacement;
  }

  private Selector replaceMiddleAppendersByEmptiness(Selector selector) {
    Selector empty = new Selector(selector.getUnderlyingStructure(), createEmptySimpleSelector(selector));
    List<Selector> replaceAppenders = replaceAppenders(selector, Arrays.asList(empty));
    Selector replacement = replaceAppenders.get(0);
    return replacement;
  }

  private Selector replaceLeadingAppendersByEmptiness(Selector selector) {
    while (!selector.isEmpty() && selector.getHead().isAppender()) {
      selector.getHead().setParent(null);
      selector.removeHead();
    }
    
    if (selector.isEmpty()) {
      SimpleSelector empty = createEmptySimpleSelector(selector);
      selector.addPart(empty);
    }
    return selector;
  }

  private SimpleSelector createEmptySimpleSelector(ASTCssNode underlyingStructureSource) {
    SimpleSelector empty = new SimpleSelector(underlyingStructureSource.getUnderlyingStructure(), null, null, true);
    empty.setEmptyForm(true);
    return empty;
  }


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
    return indirectJoinNoClone(first, combinator, second.getParts(), second.getExtend());
  }

  public Selector indirectJoinNoClone(Selector first, SelectorCombinator combinator, List<SelectorPart> second, List<Extend> extend) {
    first.addExtends(extend);

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
    List<Extend> secondExtends = ArraysUtils.deeplyClonedList(secondI.getExtend());
    SelectorPart secondHead = secondParts.get(0);

    if (secondHead.isAppender())
      return indirectJoinNoClone(first, secondHead.getLeadingCombinator(), secondParts, secondExtends);

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
    SelectorCombinator leadingCombinator = secondParts.isEmpty() ? null : secondParts.get(0).getLeadingCombinator();
    return indirectJoinNoClone(first, leadingCombinator, secondParts, secondExtends);
  }

  public void directlyJoinParts(SelectorPart first, SelectorPart second) {
    if (second.hasElement()) {
      String secondName = second.hasElement() ? second.getElementName().getName() : "";
      if (first.hasSubsequent()) {
        ElementSubsequent subsequent = first.getLastSubsequent();
        subsequent.extendName(secondName);
      } else {
        first.extendName(secondName, second.getUnderlyingStructure());
      }
    }

    first.addSubsequent(second.getSubsequent());
    first.configureParentToAllChilds();
  }

  private Collection<Selector> replaceFirstAppender(Selector selector, List<Selector> previousSelectors) {
    if (selector.getHead().isAppender()) {
      NestedSelectorAppender appender = (NestedSelectorAppender) selector.getHead();
      Selector reminder = chopOffHead(selector);
      return joinAll(previousSelectors, reminder, appender.getLeadingCombinator(), isDirectlyAfterPreviousPart(reminder));
    }

    // appender somewhere in the middle
    NestedSelectorAppender appender = selector.findFirstAppender();
    if (appender == null)
      throw new BugHappened("This is very weird error and should not happen.", selector);

    Selector afterAppender = splitOn(selector, appender);
    List<Selector> partialResults = joinAll(selector, previousSelectors, appender.getLeadingCombinator(), appender.isDirectlyAfter());
    //FIXME (now) last parameter should be nide and repats cold in 146
    return joinAll(partialResults, afterAppender, null, isDirectlyAfterPreviousPart(afterAppender));
  }

  private boolean isDirectlyAfterPreviousPart(Selector selector) {
    return selector == null ? false : !selector.hasLeadingCombinator();
  }

  private List<Selector> joinAll(Selector first, List<Selector> seconds, SelectorCombinator leadingCombinator, boolean appenderDirectlyPlaced) {
    // pretending null as after appender, less.js does not handle this case
    // anyway
    // this case being:
    // .input-group-addon + {
    // .selector {
    // heeej: hoou;
    // }
    // }
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
    boolean result = beforeAppenderCombinator == null && appenderDirectlyPlaced && (afterAppender == null || !afterAppender.hasLeadingCombinator());
    return result;
  }

  private Selector splitOn(Selector selector, NestedSelectorAppender appender) {
    List<SelectorPart> parts = selector.getParts();
    int indexOfAppender = parts.indexOf(appender);
    List<SelectorPart> appenderAndAfter = parts.subList(indexOfAppender, parts.size());

    // remove appender
    appenderAndAfter.remove(0);
    appender.setParent(null);

    // create selector with after appender parts
    Selector result = null;
    if (!appenderAndAfter.isEmpty()) {
      result = new Selector(selector.getUnderlyingStructure(), new ArrayList<SelectorPart>(appenderAndAfter));
      result.configureParentToAllChilds();
    }

    // leave only before appender parts in original selector
    appenderAndAfter.clear();
    return result;
  }

}
