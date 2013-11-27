package com.github.sommeri.less4j.core.compiler.selectors;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ElementSubsequent;
import com.github.sommeri.less4j.core.ast.SelectorCombinator;
import com.github.sommeri.less4j.core.ast.SimpleSelector;
import com.github.sommeri.less4j.core.compiler.stages.AstLogic;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
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
    return firstSubsequent.size() == secondSubsequent.size();
  }

  @Override
  public boolean prefix(SimpleSelector lookFor, SimpleSelector inside) {
    if (!combinatorsEqual(lookFor.getLeadingCombinator(), inside.getLeadingCombinator()))
      return false;

    if (!lookFor.hasSubsequent() && !lookFor.hasElement())
      return true;

    if (!simpleSelectorsElementsEqual(lookFor, inside))
      return false;

    List<ElementSubsequent> lookForSubsequent = lookFor.getSubsequent();
    List<ElementSubsequent> inSelectorSubsequent = inside.getSubsequent();
    return listsComparator.prefix(lookForSubsequent, inSelectorSubsequent, elementSubsequentComparator);
  }

  @Override
  public boolean suffix(SimpleSelector lookFor, SimpleSelector inside) {
    //combinator element subsequent
    boolean hasCombinator = AstLogic.hasNonSpaceCombinator(lookFor);
    boolean hasElement = !hasNoElement(lookFor);

    // lookFor starts with a combinator - whole selector must match
    if (hasCombinator) {
      return equals(lookFor, inside);
    }
    // lookFor starts with an element - element and subsequents must match
    if (hasElement) {
      if (!simpleSelectorsElementsEqual(lookFor, inside) || lookFor.getSubsequent().size() != inside.getSubsequent().size()) {
        return false;
      }
    }
    //compare subsequents
    if (!lookFor.hasSubsequent())
      return true;
    
    return listsComparator.suffix(lookFor.getSubsequent(), inside.getSubsequent(), elementSubsequentComparator);
  }

  /**
   * Assumes there is common suffix, e.g. suffix(lookFor, inside) returns true; 
   */
  public SimpleSelector cutSuffix(SimpleSelector cutOff, SimpleSelector inside) {
    boolean hasCombinator = AstLogic.hasNonSpaceCombinator(cutOff);
    boolean hasElement = !hasNoElement(cutOff);

    // lookFor starts with a combinator - whole selector is to be eaten
    if (hasCombinator) {
      return null;
    }
    // lookFor starts with an element - element and subsequents are to be eaten, combinator is there to stay
    SelectorCombinator combinator = inside.hasLeadingCombinator() ? inside.getLeadingCombinator().clone() : null;
    if (hasElement) {
      SimpleSelector result = createNoElementSelector(inside.getUnderlyingStructure(), combinator);
      return result;

    }
    // only some subsequents are to be eaten
    //find matches 
    List<ElementSubsequent> lfSubsequent = cutOff.getSubsequent();
    List<ElementSubsequent> insideSubsequent = inside.getSubsequent();
    MatchMarker<ElementSubsequent> match = listsComparator.suffixMatches(lfSubsequent, insideSubsequent, elementSubsequentComparator);
    SimpleSelector result = removeMatch(inside, insideSubsequent, match);
    //check if the whole thing was eaten up - we know that subsequent elements are always compared in whole
    return isEmpty(result) ? null : result;
  }

  private SimpleSelector removeMatch(SimpleSelector owner, List<ElementSubsequent> subsequentsList, MatchMarker<ElementSubsequent> match) {
    //match not found - this should not happen
    if (match == null)
      return owner;
    //remove everything that follows first from subsequent list
    int indexOfFirst = subsequentsList.indexOf(match.getFirst());
    int indexOfLast = subsequentsList.indexOf(match.getLast());
    List<ElementSubsequent> subList = subsequentsList.subList(indexOfFirst, indexOfLast + 1);
    for (ElementSubsequent elementSubsequent : subList) {
      elementSubsequent.setParent(null);
    }
    subList.removeAll(subList);
    return owner;
  }

  private boolean isEmpty(SimpleSelector owner) {
    return !AstLogic.hasNonSpaceCombinator(owner) && hasNoElement(owner) && !owner.hasSubsequent();
  }

  /**
   * Assumes there is common suffix, e.g. prefix(lookFor, inside) returns true; 
   */
  public SimpleSelector cutPrefix(SimpleSelector cutOff, SimpleSelector inside) {
    inside.setStar(true);
    inside.setEmptyForm(true);
    if (inside.hasElement())
      inside.getElementName().setParent(null);
    inside.setElementName(null);
    List<ElementSubsequent> lfSubsequent = cutOff.getSubsequent();
    List<ElementSubsequent> insideSubsequent = inside.getSubsequent();
    MatchMarker<ElementSubsequent> match = listsComparator.prefixMatches(lfSubsequent, insideSubsequent, elementSubsequentComparator);
    SimpleSelector result = removeMatch(inside, insideSubsequent, match);
    //check if the whole thing was eaten up - we know that subsequent elements are always compared in whole
    return isEmpty(result) ? null : result;
  }

  private boolean hasNoElement(SimpleSelector selector) {
    return selector.isStar() && selector.isEmptyForm();
  }

  private boolean simpleSelectorsElementsEqual(SimpleSelector first, SimpleSelector second) {
    if (first.isStar() != second.isStar())
      return false;

    if (first.isEmptyForm() != second.isEmptyForm())
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

  public SimpleSelector[] splitOn(SimpleSelector lookFor, SimpleSelector inside) {
    if (hasNoElement(lookFor)) {
      //FIXME: (!!!!) test na tento flow!!!
      //FIXME: (!!!!) move to list comparator -what did I meant by that?
      List<ElementSubsequent> subsequents = inside.getSubsequent();
      HiddenTokenAwareTree underlying = inside.getUnderlyingStructure();

      List<MatchMarker<ElementSubsequent>> matches = listsComparator.findMatches(lookFor.getSubsequent(), subsequents, elementSubsequentComparator);
      List<SimpleSelector> result = new ArrayList<SimpleSelector>();
      result.add(inside);
      for (MatchMarker<ElementSubsequent> current : matches)
        if (current.isIn(subsequents)) {
          List<ElementSubsequent> tail = splitAfter(current.getLast(), subsequents);
          removeMatch(inside, subsequents, current);
          SimpleSelector second = createNoElementSelector(underlying, tail);
          result.add(second);
          subsequents = second.getSubsequent();
        }

      return result.toArray(new SimpleSelector[0]);
    } else {
      //lookFor starts by a star or by element name - lookFor must me prefix
      return new SimpleSelector[] { null, cutPrefix(lookFor, inside) };
    }
  }

  private SimpleSelector createNoElementSelector(HiddenTokenAwareTree underlying, SelectorCombinator combinator) {
    SimpleSelector second = createEmptySelector(underlying);
    second.setLeadingCombinator(combinator);
    second.configureParentToAllChilds();
    return second;
  }

  private SimpleSelector createNoElementSelector(HiddenTokenAwareTree underlying, List<ElementSubsequent> tail) {
    SimpleSelector second = createEmptySelector(underlying);
    second.addSubsequent(tail);
    second.configureParentToAllChilds();
    return second;
  }

  private SimpleSelector createEmptySelector(HiddenTokenAwareTree underlying) {
    SimpleSelector second = new SimpleSelector(underlying, null, null, true);
    second.setEmptyForm(true);
    return second;
  }

  private List<ElementSubsequent> splitAfter(ElementSubsequent element, List<ElementSubsequent> list) {
    int indx = list.indexOf(element) + 1;
    if (indx == -1)
      indx = list.size();

    List<ElementSubsequent> subList = list.subList(indx, list.size());
    List<ElementSubsequent> result = new ArrayList<ElementSubsequent>(subList);
    subList.clear();
    return result;
  }

}
