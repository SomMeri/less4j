package com.github.sommeri.less4j.utils;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CustomAssertions {

  public static void assertSetsEquals(Set<String> expected, Set<String> actual) {
    assertEquals(expected, actual);
  }

  public static void assertEqualsAsSets(Collection<String> expected, Collection<String> actual) {
    assertEquals(new HashSet<String>(expected), new HashSet<String>(actual));
  }

}
