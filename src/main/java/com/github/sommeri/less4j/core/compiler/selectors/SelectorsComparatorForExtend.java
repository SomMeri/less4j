package com.github.sommeri.less4j.core.compiler.selectors;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ElementSubsequent;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.ast.SelectorCombinator.Combinator;
import com.github.sommeri.less4j.core.ast.SelectorPart;
import com.github.sommeri.less4j.utils.ArraysUtils;
import com.github.sommeri.less4j.utils.ListsComparator;
import com.github.sommeri.less4j.utils.ListsComparator.MatchMarker;

public class SelectorsComparatorForExtend {

  private SelectorsManipulator manipulator = new SelectorsManipulator();
  private SelectorsComparatorUtils utils = new SelectorsComparatorUtils();
  private ListsComparator listsComparator = new ListsComparator();

  private ElementSubsequentComparator elementSubsequentComparator;
  private SimpleSelectorComparator simpleSelectorComparator;
  private SelectorPartComparator selectorPartsComparator;

  public SelectorsComparatorForExtend(GeneralComparatorForExtend generalComparator) {
    this.elementSubsequentComparator = new ElementSubsequentComparator(generalComparator, utils);
    this.simpleSelectorComparator = new SimpleSelectorComparator(elementSubsequentComparator, utils);
    this.selectorPartsComparator = new SelectorPartComparator(simpleSelectorComparator);
  }

  public boolean equals(Selector first, Selector second) {
    List<SelectorPart> firstParts = first.getParts();
    List<SelectorPart> secondParts = second.getParts();
    return listsComparator.equals(firstParts, secondParts, selectorPartsComparator);
  }

  public boolean contains(Selector lookFor, Selector inSelector) {
    List<SelectorPart> lookForParts = lookFor.getParts();
    List<SelectorPart> inSelectorParts = inSelector.getParts();

    return replaceInList(lookForParts, inSelectorParts, null) || containsEmbedded(lookForParts, inSelectorParts);
  }

  public boolean replace(Selector lookFor, Selector inSelector, Selector replaceBy) {
    List<SelectorPart> lookForParts = lookFor.getParts();
    List<SelectorPart> inSelectorParts = inSelector.getParts();

    return replaceInList(lookForParts, inSelectorParts, replaceBy.getParts()) || containsEmbedded(lookForParts, inSelectorParts);
  }

  private boolean replaceInList(List<SelectorPart> lookForParts, List<SelectorPart> inSelectorParts, List<SelectorPart> replaceBy) {
    boolean contains = listsComparator.contains(lookForParts, inSelectorParts, selectorPartsComparator);

    if (contains && replaceBy!=null && !replaceBy.isEmpty()) {
      Selector parentSelector = inSelectorParts.get(0).getParentAsSelector();
      lookForParts = ArraysUtils.deeplyClonedList(lookForParts);
      replaceBy = ArraysUtils.deeplyClonedList(replaceBy);
      List<MatchMarker<SelectorPart>> matches = listsComparator.findMatches(lookForParts, inSelectorParts, selectorPartsComparator);

      List<SelectorPart> newBeginning = null;
      List<SelectorPart> newMiddle = replaceBy;
      List<SelectorPart> newEnding = null;
      List<SelectorPart> removeThese = null;

      SelectorPart firstMatch = matches.get(0).getFirst();
      int firstIndex = inSelectorParts.indexOf(firstMatch);
      int firstUnusedReplaceBy = 0;
      SelectorPart firstRemainder = selectorPartsComparator.cutSuffix(lookForParts.get(0), firstMatch);
      if (firstRemainder != null) {
        firstIndex++;
        firstUnusedReplaceBy++;
        SelectorPart firstReplaceBy = ArraysUtils.first(replaceBy);
        //FIXME (!!!) document less.js ignores leading and final combinators
        manipulator.directlyJoinParts(firstRemainder, firstReplaceBy);
      }
      newBeginning = inSelectorParts.subList(0, firstIndex);

      SelectorPart lastMatch = matches.get(0).getLast();
      int lastIndex = inSelectorParts.indexOf(lastMatch);
      int lastUnusedReplaceBy = replaceBy.size();
      SelectorPart lastRemainder = selectorPartsComparator.cutPrefix(ArraysUtils.last(lookForParts), lastMatch);
      if (lastRemainder != null) {
        SelectorPart lastReplaceBy = ArraysUtils.last(replaceBy);
        if (replaceBy.size() != 1) {
          lastUnusedReplaceBy--;
        } else {
          lastIndex++;
          if (firstRemainder!=null)
            lastReplaceBy = firstRemainder;
        }
        manipulator.directlyJoinParts(lastReplaceBy, lastRemainder);
      }
      removeThese = sublistCopy(inSelectorParts, firstIndex, lastIndex);
      newEnding = sublistCopy(inSelectorParts, lastIndex, inSelectorParts.size());

      newMiddle = sublistCopy(replaceBy, firstUnusedReplaceBy, lastUnusedReplaceBy);
      //FIXME: test with combinator before and in the end
      //FIXME: what if it has only one part
      //lets split it into three lists and connect them then

      //split into two selectors and then join the things
      //FIXME (!!!) extract to some ast manipulator - it is also in simple selector
      for (SelectorPart elementSubsequent : removeThese) {
        elementSubsequent.setParent(null);
      }
      removeThese.clear();

      @SuppressWarnings("unchecked")
      List<SelectorPart> newParts = ArraysUtils.joinAll(newBeginning, newMiddle, newEnding);

      parentSelector.setParts(newParts);
      parentSelector.configureParentToAllChilds();
    }

    return contains;
  }

  private List<SelectorPart> sublistCopy(List<SelectorPart> list, int first, int last) {
    return (first > last) ? new ArrayList<SelectorPart>() : new ArrayList<SelectorPart>(list.subList(first, last));
  }

  private boolean containsEmbedded(List<SelectorPart> lookFor, List<SelectorPart> inSelectors) {
    for (SelectorPart inside : inSelectors) {
      if (containsEmbedded(lookFor, inside))
        return true;
    }
    return false;
  }

  private boolean containsEmbedded(List<SelectorPart> lookFor, ASTCssNode inside) {
    for (ASTCssNode kid : inside.getChilds()) {
      switch (kid.getType()) {
      case SELECTOR:
        Selector kidSelector = (Selector) kid;
        if (replaceInList(lookFor, kidSelector.getParts(), null))
          return true;
        break;
      default:
        if (containsEmbedded(lookFor, kid))
          return true;
      }
    }
    return false;
  }

}
