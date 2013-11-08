package com.github.sommeri.less4j.core.compiler.selectors;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.ast.SelectorPart;
import com.github.sommeri.less4j.utils.ArraysUtils;
import com.github.sommeri.less4j.utils.ListsComparator;
import com.github.sommeri.less4j.utils.ListsComparator.MatchMarker;

public class Old_SelectorsComparatorForExtend {

  private SelectorsManipulator manipulator = new SelectorsManipulator();
  private SelectorsComparatorUtils utils = new SelectorsComparatorUtils();
  private ListsComparator listsComparator = new ListsComparator();

  private ElementSubsequentComparator elementSubsequentComparator;
  private SimpleSelectorComparator simpleSelectorComparator;
  private SelectorPartComparator selectorPartsComparator;

  public Old_SelectorsComparatorForExtend(GeneralComparatorForExtend generalComparator) {
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

    return containsInList(lookForParts, inSelectorParts) || containsEmbedded(lookForParts, inSelectorParts);
  }

  public Selector replace(Selector lookFor, Selector inSelector, Selector replaceBy) {
    List<SelectorPart> lookForParts = lookFor.getParts();
    List<SelectorPart> inSelectorParts = ArraysUtils.deeplyClonedList(inSelector.getParts());

    Selector result = new Selector(inSelector.getUnderlyingStructure(), inSelectorParts);
    replaceInList(lookForParts, result, replaceBy.getParts());
    //replaceEmbedded(lookForParts, result); //FIXME: (!!!!) replace in embedded || ;
    return  result;
  }

  private boolean containsInList(List<SelectorPart> lookForParts, List<SelectorPart> inSelectorParts) {
    boolean contains = listsComparator.contains(lookForParts, inSelectorParts, selectorPartsComparator);
    return contains;
  }

  //FIXME: (!!!) test with combinator before and in the end
  //FIXME (!!!) document less.js ignores leading and final combinators
  private Selector replaceInList(List<SelectorPart> lookForParts, Selector inSelector, List<SelectorPart> replaceBy) {
    List<SelectorPart> inSelectorParts = inSelector.getParts();
    List<SelectorPart> newInSelectorParts = new ArrayList<SelectorPart>();
    
    List<MatchMarker<SelectorPart>> matches = listsComparator.findMatches(lookForParts, inSelectorParts, selectorPartsComparator);
    if (matches.isEmpty() || replaceBy == null || replaceBy.isEmpty())
      return null;

    //underlying
    lookForParts = ArraysUtils.deeplyClonedList(lookForParts);
    replaceBy = ArraysUtils.deeplyClonedList(replaceBy);

    List<SelectorPart> newBeginning = null;
    List<SelectorPart> newEnding = null;
    List<SelectorPart> removeThese = null;

    SelectorPart firstMatch = matches.get(0).getFirst();
    SelectorPart lastMatch = matches.get(0).getLast();
    
//    if (firstMatch==lastMatch)
//      throw new IllegalStateException("Not really implemented");
    
    int firstIndex = inSelectorParts.indexOf(firstMatch);
    //newInSelectorParts.addAll(chopPrefix(inSelectorParts.subList(0, firstIndex)));
    
    SelectorPart firstRemainder = selectorPartsComparator.cutSuffix(lookForParts.get(0), firstMatch);
    if (firstRemainder != null) {
      firstIndex++;
      SelectorPart firstReplaceBy = replaceBy.remove(0);
      manipulator.directlyJoinParts(firstRemainder, firstReplaceBy);
    }
    newBeginning = inSelectorParts.subList(0, firstIndex);

    int lastIndex = inSelectorParts.indexOf(lastMatch);

    SelectorPart lastRemainder = lastMatch==firstMatch? null : selectorPartsComparator.cutPrefix(ArraysUtils.last(lookForParts), lastMatch);
    //SelectorPart lastRemainder = selectorPartsComparator.cutPrefix(ArraysUtils.last(lookForParts), lastMatch);
    if (lastRemainder != null) {
      if (replaceBy.isEmpty()) {
        manipulator.directlyJoinParts(firstRemainder, lastRemainder);
      } else {
        SelectorPart lastReplaceBy = ArraysUtils.last(replaceBy);
        manipulator.directlyJoinParts(lastReplaceBy, lastRemainder);
      }
    } 
    lastIndex++;
    removeThese = sublistCopy(inSelectorParts, firstIndex, lastIndex);
    newEnding = sublistCopy(inSelectorParts, lastIndex, inSelectorParts.size());

    //FIXME (!!!) extract to some ast manipulator - it is also in simple selector comparator
    removeFromParent(removeThese);

    @SuppressWarnings("unchecked")
    List<SelectorPart> newParts = ArraysUtils.joinAll(newBeginning, replaceBy, newEnding);

    inSelector.setParts(newParts);
    inSelector.configureParentToAllChilds();

    return inSelector;
  }

  private void removeFromParent(List<SelectorPart> removeThese) {
    for (SelectorPart elementSubsequent : removeThese) {
      elementSubsequent.setParent(null);
    }
    removeThese.clear();
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
        if (containsInList(lookFor, kidSelector.getParts()))
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
