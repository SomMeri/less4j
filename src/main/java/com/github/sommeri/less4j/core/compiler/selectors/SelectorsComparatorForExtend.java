package com.github.sommeri.less4j.core.compiler.selectors;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.ast.SelectorPart;
import com.github.sommeri.less4j.core.problems.BugHappened;
import com.github.sommeri.less4j.utils.ArraysUtils;
import com.github.sommeri.less4j.utils.ListsComparator;
import com.github.sommeri.less4j.utils.ListsComparator.MatchMarker;

public class SelectorsComparatorForExtend {

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

    return containsInList(lookForParts, inSelectorParts) || containsEmbedded(lookForParts, inSelectorParts);
  }

  public Selector replace(Selector lookFor, Selector inSelector, Selector replaceBy) {
    List<SelectorPart> lookForParts = lookFor.getParts();
    List<SelectorPart> inSelectorParts = ArraysUtils.deeplyClonedList(inSelector.getParts());

    Selector result = new Selector(inSelector.getUnderlyingStructure(), inSelectorParts);
    replaceInList(lookForParts, result, replaceBy.getParts());
    //replaceEmbedded(lookForParts, result); //FIXME: (!!!!) replace in embedded || ;
    return result;
  }

  private boolean containsInList(List<SelectorPart> lookForParts, List<SelectorPart> inSelectorParts) {
    boolean contains = listsComparator.contains(lookForParts, inSelectorParts, selectorPartsComparator);
    return contains;
  }

  //FIXME: (!!!) test with combinator before and in the end
  private Selector replaceInList(List<SelectorPart> lookForParts, Selector inSelector, List<SelectorPart> originalReplaceBy) {
    List<SelectorPart> inSelectorParts = inSelector.getParts();
    SelectorPartsListBuilder builder = new SelectorPartsListBuilder();

    List<MatchMarker<SelectorPart>> matches = listsComparator.findMatches(lookForParts, inSelectorParts, selectorPartsComparator);
    if (matches.isEmpty() || originalReplaceBy == null || originalReplaceBy.isEmpty())
      return null;

    SelectorPart lastRemainder = null;
    MatchMarker<SelectorPart> previousMatch = null;
    for (MatchMarker<SelectorPart> currentMatch : matches) {
      SelectorPart firstMatch = currentMatch.getFirst();
      SelectorPart lastMatch = currentMatch.getLast();
      
      if (!inSelectorParts.contains(firstMatch) && (previousMatch==null || lastRemainder==null ||  previousMatch.getLast()!=currentMatch.getFirst())) { //particularly ugly condition
        previousMatch = currentMatch;
        continue ;
      }
      boolean prefixNeeded = true;
      
      if (!inSelectorParts.contains(firstMatch) && lastRemainder!=null) { //particularly ugly condition
      //FIXME: !!!!!!!!!!!!!!!!!!! ugly -I need this when it is partial match and need to skip out if it is full match
        firstMatch = ArraysUtils.last(builder.getParts()); 
        prefixNeeded = false;
      }
      
      previousMatch = currentMatch;
      List<SelectorPart> replaceBy = ArraysUtils.deeplyClonedList(originalReplaceBy);

      if (firstMatch == lastMatch) {
        if (lookForParts.size() != 1)
          throw new BugHappened("Impossible state happened.", lookForParts.isEmpty() ? null : lookForParts.get(0));

        List<SelectorPart> replaceInside = replaceInsidePart(lookForParts.get(0), lastMatch, replaceBy);
        ArraysUtils.replace(lastMatch, inSelectorParts, replaceInside);
      } else {
        if (prefixNeeded)
          builder.addUpTo(inSelectorParts, firstMatch);
        ArraysUtils.chopFirst(inSelectorParts); // now we are chopping firstMatch

        SelectorPart firstRemainder = selectorPartsComparator.cutSuffix(lookForParts.get(0), firstMatch);
        if (firstRemainder != null) {
          if (prefixNeeded)
            builder.add(firstRemainder);
          builder.directlyAttach(replaceBy);
        } else {
          builder.addAll(replaceBy);
        }

        removeFromParent(ArraysUtils.chopUpTo(inSelectorParts, lastMatch));
        ArraysUtils.chopFirst(inSelectorParts); // now we are chopping lastMatch

        lastRemainder = selectorPartsComparator.cutPrefix(ArraysUtils.last(lookForParts), lastMatch);
        builder.directlyAttachNonNull(lastRemainder);
      }
    }

    builder.addAll(inSelectorParts);
    inSelector.setParts(builder.getParts());
    inSelector.configureParentToAllChilds();

    return inSelector;
  }

  private List<SelectorPart> replaceInsidePart(SelectorPart lookFor, SelectorPart inside, List<SelectorPart> replaceBy) {
    SelectorPart[] split = selectorPartsComparator.splitOn(lookFor, inside);
    SelectorPartsListBuilder builder = new SelectorPartsListBuilder();
    if (split.length > 0) {
      builder.directlyAttachNonNull(split[0]);
      for (int i = 1; i < split.length; i++) {
        builder.directlyAttach(ArraysUtils.deeplyClonedList(replaceBy));
        builder.directlyAttachNonNull(split[i]);
      }
    }
    return builder.getParts();
  }

  private void removeFromParent(List<SelectorPart> removeThese) {
    for (SelectorPart elementSubsequent : removeThese) {
      elementSubsequent.setParent(null);
    }
    removeThese.clear();
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

class SelectorPartsListBuilder {

  private SelectorsManipulator manipulator = new SelectorsManipulator();
  private List<SelectorPart> newInSelectorParts = new ArrayList<SelectorPart>();

  public SelectorPartsListBuilder() {
  }

  public List<SelectorPart> getParts() {
    return newInSelectorParts;
  }

  public void directlyAttachNonNull(SelectorPart part) {
    if (part != null)
      directlyAttach(part);
  }

  public void directlyAttach(SelectorPart part) {
    if (newInSelectorParts.isEmpty()) {
      add(part);
    } else {
      SelectorPart tail = ArraysUtils.last(newInSelectorParts);
      manipulator.directlyJoinParts(tail, part);
    }
  }

  public void directlyAttach(List<SelectorPart> list) {
    if (!newInSelectorParts.isEmpty()) {
      SelectorPart tail = ArraysUtils.last(newInSelectorParts);
      SelectorPart firstReplaceBy = list.remove(0);
      manipulator.directlyJoinParts(tail, firstReplaceBy);
    }
    addAll(list);
  }

  public void addAll(List<SelectorPart> list) {
    newInSelectorParts.addAll(list);
  }

  public void addNonNull(SelectorPart part) {
    if (part != null)
      add(part);
  }

  public void add(SelectorPart part) {
    newInSelectorParts.add(part);
  }

  public void addUpTo(List<SelectorPart> inSelectorParts, SelectorPart firstMatch) {
    newInSelectorParts.addAll(ArraysUtils.chopUpTo(inSelectorParts, firstMatch));
  }

  @Override
  public String toString() {
    return newInSelectorParts.toString();
  }

}
