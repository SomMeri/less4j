package com.github.sommeri.less4j.core.compiler.selectors;

import java.util.List;

import com.github.sommeri.less4j.core.ast.ElementSubsequent;
import com.github.sommeri.less4j.core.ast.SelectorCombinator;
import com.github.sommeri.less4j.core.ast.SimpleSelector;
import com.github.sommeri.less4j.utils.ArraysUtils;
import com.github.sommeri.less4j.utils.ListsComparator;
import com.github.sommeri.less4j.utils.ListsComparator.ListMemberComparator;
import com.github.sommeri.less4j.utils.ListsComparator.MatchMarker;

//FIXME: (!!!!) this whole class mostly ignores leading combinators - need special logic for them
public class SimpleSelectorComparator implements ListMemberComparator<SimpleSelector> {

  private final SelectorsComparatorUtils utils;
  private final ElementSubsequentComparator elementSubsequentComparator;
  private final ListsComparator listsComparator = new ListsComparator();

  public SimpleSelectorComparator(ElementSubsequentComparator elementSubsequentComparator, SelectorsComparatorUtils utils) {
    super();
    this.utils = utils;
    this.elementSubsequentComparator = elementSubsequentComparator;
  }

  @Override
  public boolean equals(SimpleSelector first, SimpleSelector second) {
    if (!prefix(first, second))
      return false;
    
    List<ElementSubsequent> firstSubsequent = first.getSubsequent();
    List<ElementSubsequent> secondSubsequent = second.getSubsequent();
    return firstSubsequent.size()==secondSubsequent.size();
  }

  @Override
  public boolean prefix(SimpleSelector lookFor, SimpleSelector inside) {
    if (!simpleSelectorsElementsEqual(lookFor, inside))
      return false;

    List<ElementSubsequent> lookForSubsequent = lookFor.getSubsequent();
    List<ElementSubsequent> inSelectorSubsequent = inside.getSubsequent();
    return listsComparator.prefix(lookForSubsequent, inSelectorSubsequent, elementSubsequentComparator);
  }

  @Override
  public boolean suffix(SimpleSelector lookFor, SimpleSelector inside) {
    if (hasNoElement(lookFor)) {
      return listsComparator.suffix(lookFor.getSubsequent(), inside.getSubsequent(), elementSubsequentComparator);
    } else {
      //lookFor starts by a star or by element name - whole selector must match
      return equals(lookFor, inside);
    }
  }

  /**
   * FIXME (!!!!) test and clean up - ugly
   * Assumes there is common suffix, e.g. suffix(lookFor, inside) returns true; 
   */
  public SimpleSelector cutSuffix(SimpleSelector lookFor, SimpleSelector inside) {
    if (hasNoElement(lookFor)) {
      //find matches 
      List<ElementSubsequent> lfSubsequent = lookFor.getSubsequent();
      List<ElementSubsequent> insideSubsequent = inside.getSubsequent();
      MatchMarker<ElementSubsequent> match = listsComparator.suffixMatches(lfSubsequent, insideSubsequent, elementSubsequentComparator);
      return removeMatch(inside, insideSubsequent, match);
    } else {
      return null;
    }
  }

  private SimpleSelector removeMatch(SimpleSelector owner, List<ElementSubsequent> subsequentsList, MatchMarker<ElementSubsequent> match) {
    //match not found - this should not happen
    if (match==null)
      return owner;
    //whole thing was eaten up - we know that subsequent elements are always compared in whole 
    if (hasNoElement(owner) && match.getFirst()==subsequentsList.get(0) && match.getLast()==ArraysUtils.last(subsequentsList))
      return null;
    //remove everything that follows first from subsequent list
    int indexOfFirst = subsequentsList.indexOf(match.getFirst());
    int indexOfLast = subsequentsList.indexOf(match.getFirst());
    List<ElementSubsequent> subList = subsequentsList.subList(indexOfFirst, indexOfLast+1);
    for (ElementSubsequent elementSubsequent : subList) {
      elementSubsequent.setParent(null);
    }
    subList.removeAll(subList);
    return owner;
  }

  /**
   * FIXME (!!!!) only part of the name should be cut, not the whole think!! the same with prefix math
   * Assumes there is common suffix, e.g. prefix(lookFor, inside) returns true; 
   */
  public SimpleSelector cutPrefix(SimpleSelector lookFor, SimpleSelector inside) {
    inside.setStar(true);
    inside.setEmptyForm(true);
    if (inside.hasElement())
      inside.getElementName().setParent(null);
    inside.setElementName(null);
    List<ElementSubsequent> lfSubsequent = lookFor.getSubsequent();
    List<ElementSubsequent> insideSubsequent = inside.getSubsequent();
    MatchMarker<ElementSubsequent> match = listsComparator.prefixMatches(lfSubsequent, insideSubsequent, elementSubsequentComparator);
    return removeMatch(inside, insideSubsequent, match);
  }

  private boolean hasNoElement(SimpleSelector selector) {
    return selector.isStar() && selector.isEmptyForm();
  }

  private boolean simpleSelectorsElementsEqual(SimpleSelector first, SimpleSelector second) {
    if (first.isStar() != second.isStar())
      return false;

    if (first.isEmptyForm() != second.isEmptyForm())
      return false;

    if (!combinatorsEqual(first.getLeadingCombinator(), second.getLeadingCombinator()))
      return false;

    if (!utils.interpolableNamesEqual(first.getElementName(), second.getElementName()))
      return false;

    return true;
  }

  private boolean combinatorsEqual(SelectorCombinator cmb1, SelectorCombinator cmb2) {
    //FIXME: (!!!) this is here to solve special case, leading combinators must be rethingg
    // the problem is that 
    /* 
     * h1 {} has empty leading combinator
     * and 
     * :extend(h1) does not have
     */
    if (cmb1 == null)
      return cmb2 == null || cmb2.getCombinator() == SelectorCombinator.Combinator.DESCENDANT;

    if (cmb2 == null)
      return cmb1 == null || cmb1.getCombinator() == SelectorCombinator.Combinator.DESCENDANT;

    return cmb1.getCombinator() == cmb2.getCombinator();
  }

  public boolean contains(SimpleSelector lookFor, SimpleSelector inside) {
    if (hasNoElement(lookFor)) {
      return listsComparator.contains(lookFor.getSubsequent(), inside.getSubsequent(), elementSubsequentComparator);
    } else {
      //lookFor starts by a star or by element name - lookFor must me prefix
      return prefix(lookFor, inside);
    }
  }

}
