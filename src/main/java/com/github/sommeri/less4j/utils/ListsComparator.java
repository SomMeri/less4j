package com.github.sommeri.less4j.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ElementSubsequent;

public class ListsComparator {

  public <T> boolean equals(List<T> first, List<T> second, ListMemberComparator<T> comparator) {
    if (first.size() != second.size())
      return false;

    Iterator<T> i1 = first.iterator();
    Iterator<T> i2 = second.iterator();
    while (i1.hasNext()) {
      T firstMember = i1.next();
      T secondMember = i2.next();
      if (!comparator.equals(firstMember, secondMember))
        return false;
    }
    return true;
  }

  public <T> boolean prefix(List<T> lookFor, List<T> inList, ListMemberComparator<T> comparator) {
    if (lookFor.isEmpty())
      return true;

    if (lookFor.size() > inList.size())
      return false;

    List<T> relevantInList = ArraysUtils.sameLengthPrefix(inList, lookFor);
    List<T> relevantInListWithoutLast = ArraysUtils.sublistWithoutLast(relevantInList);
    List<T> lookForWithoutLast = ArraysUtils.sublistWithoutLast(lookFor);

    if (!equals(lookForWithoutLast, relevantInListWithoutLast, comparator))
      return false;

    return comparator.prefix(ArraysUtils.last(lookFor), ArraysUtils.last(relevantInList));
  }

  public <T> MatchMarker<T> prefixMatches(List<T> lookFor, List<T> inList, ListMemberComparator<T> comparator) {
    if (lookFor.isEmpty() || lookFor.size() > inList.size())
      return null;

    List<T> relevantInList = ArraysUtils.sameLengthPrefix(inList, lookFor);
    List<T> relevantInListWithoutLast = ArraysUtils.sublistWithoutLast(relevantInList);
    List<T> lookForWithoutLast = ArraysUtils.sublistWithoutLast(lookFor);

    if (!equals(lookForWithoutLast, relevantInListWithoutLast, comparator))
      return null ;

    boolean isPrefix = comparator.prefix(ArraysUtils.last(lookFor), ArraysUtils.last(relevantInList));
    if (isPrefix)
      return new MatchMarker<T>(ArraysUtils.first(relevantInList), ArraysUtils.last(relevantInList), false);
    
    return null;
  }

  public <T> T prefixEnd(List<T> lookFor, List<T> inList, ListMemberComparator<T> comparator) {
    if (lookFor.isEmpty())
      return inList.isEmpty() ? null : inList.get(0);

    if (lookFor.size() > inList.size())
      return null;

    List<T> relevantInList = ArraysUtils.sameLengthPrefix(inList, lookFor);
    List<T> relevantInListWithoutLast = ArraysUtils.sublistWithoutLast(relevantInList);
    List<T> lookForWithoutLast = ArraysUtils.sublistWithoutLast(lookFor);

    if (!equals(lookForWithoutLast, relevantInListWithoutLast, comparator))
      return null;

    if (comparator.prefix(ArraysUtils.last(lookFor), ArraysUtils.last(relevantInList)))
      return ArraysUtils.last(relevantInList);

    return null;
  }

  public <T> boolean suffix(List<T> lookFor, List<T> inList, ListMemberComparator<T> comparator) {
    return null!=suffixMatches(lookFor, inList, comparator);
  }

  public <T> MatchMarker<T> suffixMatches(List<T> lookFor, List<T> inList, ListMemberComparator<T> comparator) {
    if (lookFor.isEmpty() || lookFor.size() > inList.size())
      return null;

    List<T> relevantInList = ArraysUtils.sameLengthSuffix(inList, lookFor);
    List<T> relevantInListWithoutFirst = ArraysUtils.sublistWithoutFirst(relevantInList);
    List<T> lookForWithoutFirst = ArraysUtils.sublistWithoutFirst(lookFor);

    if (!equals(lookForWithoutFirst, relevantInListWithoutFirst, comparator))
      return null;

    
    boolean isSuffix = comparator.suffix(ArraysUtils.first(lookFor), ArraysUtils.first(relevantInList));
    if (isSuffix)
      return new MatchMarker<T>(ArraysUtils.first(relevantInList), ArraysUtils.last(relevantInList), false);
    
    return null;
  }

  public <T> boolean contains(List<T> lookFor, List<T> inSelector, ListMemberComparator<T> comparator) {
    return !findMatches(lookFor, inSelector, comparator).isEmpty();
  }

  //FIXME: rename inSelector to inList
  public <T> List<MatchMarker<T>> findMatches(List<T> lookFor, List<T> inSelector, ListMemberComparator<T> comparator) {
    return collectMatches(lookFor, inSelector, comparator, new ArrayList<MatchMarker<T>>());
  }

  public <T> List<MatchMarker<T>> collectMatches(List<T> lookFor, List<T> inSelector, ListMemberComparator<T> comparator, List<MatchMarker<T>> result) {
    if (lookFor.isEmpty() || lookFor.size() > inSelector.size())
      return result;

    if (lookFor.size() == 1) {
      return collectMatches(ArraysUtils.first(lookFor), inSelector, comparator, result);
    }

    T firstLookFor = ArraysUtils.first(lookFor);
    T firstInSelector = ArraysUtils.first(inSelector);

    List<T> remainderLookFor = ArraysUtils.sublistWithoutFirst(lookFor);
    List<T> remainderInSelector = ArraysUtils.sublistWithoutFirst(inSelector);

    boolean firstIsSuffix = comparator.suffix(firstLookFor, firstInSelector);
    T prefixEnd = prefixEnd(remainderLookFor, remainderInSelector, comparator);
    boolean remainderIsPrefix = prefixEnd != null;
    if (firstIsSuffix && remainderIsPrefix) {
      result.add(new MatchMarker<T>(firstInSelector, prefixEnd, false));
    }

    collectMatches(lookFor, remainderInSelector, comparator, result);
    return result;
  }

  private <T> boolean containsInList(T lookFor, List<T> inParts, ListMemberComparator<T> comparator) {
    for (T inPart : inParts) {
      if (comparator.contains(lookFor, inPart))
        return true;
    }
    return false;
  }

  private <T> List<MatchMarker<T>> collectMatches(T lookFor, List<T> inParts, ListMemberComparator<T> comparator, List<MatchMarker<T>> result) {
    for (T inPart : inParts) {
      if (comparator.contains(lookFor, inPart))
        result.add(new MatchMarker<T>(inPart, inPart, false));
    }
    return result;
  }

  public static interface ListMemberComparator<T> {

    public boolean equals(T first, T second);

    public boolean prefix(T lookFor, T inside);

    public boolean suffix(T lookFor, T inside);

    public boolean contains(T lookFor, T inside);

  }

  public static interface ModifiedListBuilder<T> {

    public boolean splitInside(T lookFor, T inPart, List<T> replaceBy);

    public boolean cutSuffix(T lookFor, T inside, List<T> replaceBy);

    public boolean cutPrefix(T lookFor, T inside, List<T> replaceBy);

  }

  public class MatchMarker<T> {
    private T first;
    private T last;
    private boolean isEquals;

    public MatchMarker(T first, T last, boolean isEquals) {
      this.first = first;
      this.last = last;
      this.isEquals = isEquals;
    }

    public T getFirst() {
      return first;
    }

    public void setFirst(T first) {
      this.first = first;
    }

    public T getLast() {
      return last;
    }

    public void setLast(T last) {
      this.last = last;
    }

    public boolean isEquals() {
      return isEquals;
    }

    public void setEquals(boolean isEquals) {
      this.isEquals = isEquals;
    }

    public boolean firstIsLast() {
      return first==last;
    }

    public boolean isIn(List<T> list) {
      return list.contains(getFirst()) && list.contains(getLast());
    }
    
  }
}
