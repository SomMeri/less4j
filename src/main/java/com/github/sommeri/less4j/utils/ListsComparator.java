package com.github.sommeri.less4j.utils;

import java.util.Iterator;
import java.util.List;

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

  public <T> boolean suffix(List<T> lookFor, List<T> inList, ListMemberComparator<T> comparator) {
    if (lookFor.isEmpty())
      return true;

    if (lookFor.size() > inList.size())
      return false;
    
    List<T> relevantInList = ArraysUtils.sameLengthSuffix(inList, lookFor);
    List<T> relevantInListWithoutFirst = ArraysUtils.sublistWithoutFirst(relevantInList);
    List<T> lookForWithoutFirst = ArraysUtils.sublistWithoutFirst(lookFor);

    if (!equals(lookForWithoutFirst, relevantInListWithoutFirst, comparator))
      return false;
    
    return comparator.suffix(ArraysUtils.first(lookFor), ArraysUtils.first(relevantInList));
  }

  public <T> boolean contains(List<T> lookFor, List<T> inSelector, ListMemberComparator<T> comparator) {
    if (lookFor.isEmpty())
      return true;

    if (lookFor.size() > inSelector.size())
      return false;

    if (lookFor.size() == 1) {
      return containsInList(ArraysUtils.first(lookFor), inSelector, comparator);
    }

    T firstLookFor = ArraysUtils.first(lookFor);
    T firstInSelector = ArraysUtils.first(inSelector);

    List<T> remainderLookFor = ArraysUtils.sublistWithoutFirst(lookFor);
    List<T> remainderInSelector = ArraysUtils.sublistWithoutFirst(inSelector);

    boolean firstIsSuffix = comparator.suffix(firstLookFor, firstInSelector);
    boolean remainderIsPrefix = prefix(remainderLookFor, remainderInSelector, comparator);
    if (firstIsSuffix && remainderIsPrefix)
      return true;

    return contains(lookFor, remainderInSelector, comparator);
  }

  private <T> boolean containsInList(T lookFor, List<T> inParts, ListMemberComparator<T> comparator) {
    for (T inPart : inParts) {
      if (comparator.contains(lookFor, inPart))
        return true;
    }
    return false;
  }

  public static interface ListMemberComparator<T> {

    public boolean equals(T first, T second);
    
    public boolean prefix(T lookFor, T inside);
    
    public boolean suffix(T lookFor, T inside);
    
    public boolean contains(T lookFor, T inside);

  }
  
  public static interface ModifyingListMemberComparator<T> {
    
  }
}
