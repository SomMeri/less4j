package com.github.sommeri.less4j.utils;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomAssertions {

  public static void assertSetsEquals(Set<String> expected, Set<String> actual) {
    //FIXME: source map - better error messages in this case e.g., list missing and additional ones
    assertEquals(expected, actual);
  }

  public static void assertEqualsAsSets(List<String> expected, Collection<String> actual) {
    assertEquals(new HashSet<String>(expected), new HashSet<String>(actual));
  }

  private static boolean equalsRegardingNull(Object expected, Object actual) {
    if (expected == null) {
      return actual == null;
    }

    return isEquals(expected, actual);
  }

  private static boolean isEquals(Object expected, Object actual) {
    return expected.equals(actual);
  }

}
