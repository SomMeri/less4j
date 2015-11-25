package com.github.sommeri.less4j.utils;

import java.util.ArrayList;
import java.util.List;

public class ListsComparator {

  public <T> boolean equals(List<T> first, List<T> second, ListMemberComparator<T> comparator) {
    if (first.size() != second.size()) {
      return false;
    }
    return equals(first, second, first.size(), comparator);
  }

  public <T> boolean equals(List<T> first, List<T> second, int count, ListMemberComparator<T> comparator) {
    for (int i = 0; i < first.size() && i < count; i++) {
      if (!comparator.equals(first.get(i), second.get(i))) {
        return false;
      }
    }
    return true;
  }

  public <T> boolean prefix(List<T> lookFor, List<T> inList, ListMemberComparator<T> comparator) {
    if (lookFor.isEmpty())
      return true;

    if (lookFor.size() > inList.size())
      return false;

    int beforeLast = lookFor.size() - 1;

    if (!equals(lookFor, inList, beforeLast, comparator))
      return false;

    return comparator.prefix(lookFor.get(beforeLast), inList.get(beforeLast));
  }

  public <T> MatchMarker<T> prefixMatches(List<T> lookFor, List<T> inList, ListMemberComparator<T> comparator) {
    if (lookFor.isEmpty() || lookFor.size() > inList.size())
      return null;

    List<T> relevantInList = ArraysUtils.sameLengthPrefix(inList, lookFor);
    List<T> relevantInListWithoutLast = ArraysUtils.sublistWithoutLast(relevantInList);
    List<T> lookForWithoutLast = ArraysUtils.sublistWithoutLast(lookFor);

    if (!equals(lookForWithoutLast, relevantInListWithoutLast, comparator))
      return null;

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
    return null != suffixMatches(lookFor, inList, comparator);
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

  public <T> boolean contains(List<T> lookFor, List<T> inList, ListMemberComparator<T> comparator) {
    return !findMatches(lookFor, inList, comparator).isEmpty();
  }

  public <T> List<MatchMarker<T>> findMatches(List<T> lookFor, List<T> inList, ListMemberComparator<T> comparator) {
    return collectMatches(lookFor, inList, comparator, new ArrayList<MatchMarker<T>>());
  }

  public <T> List<MatchMarker<T>> collectMatches(List<T> lookFor, List<T> inList, ListMemberComparator<T> comparator, List<MatchMarker<T>> result) {
    if (lookFor.isEmpty() || lookFor.size() > inList.size())
      return result;

    if (lookFor.size() == 1) {
      return collectMatches(ArraysUtils.first(lookFor), inList, comparator, result);
    }

    T firstLookFor = ArraysUtils.first(lookFor);
    T firstInList = ArraysUtils.first(inList);

    List<T> remainderLookFor = ArraysUtils.sublistWithoutFirst(lookFor);
    List<T> remainderInList = ArraysUtils.sublistWithoutFirst(inList);

    boolean firstIsSuffix = comparator.suffix(firstLookFor, firstInList);
    T prefixEnd = prefixEnd(remainderLookFor, remainderInList, comparator);
    boolean remainderIsPrefix = prefixEnd != null;
    if (firstIsSuffix && remainderIsPrefix) {
      result.add(new MatchMarker<T>(firstInList, prefixEnd, false));
    }

    collectMatches(lookFor, remainderInList, comparator, result);
    return result;
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
      return first == last;
    }

    public boolean isIn(List<T> list) {
      return list.contains(getFirst()) && list.contains(getLast());
    }

  }
}
