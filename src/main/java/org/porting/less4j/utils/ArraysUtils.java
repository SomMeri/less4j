package org.porting.less4j.utils;

import java.util.ArrayList;
import java.util.List;

public class ArraysUtils {
  
  public static <T> List<T> asNonNullList(T... a) {
    List<T> result = new ArrayList<T>();
    for (T t : a) {
      if (t != null)
        result.add(t);
    }
    return result;
  }

  public static <T> List<T> clonedList(List<T> list) {
    List<T> result = new ArrayList<T>();
    for (T t : list) {
      result.add(t);
    }
    return result;
  }
  
}
