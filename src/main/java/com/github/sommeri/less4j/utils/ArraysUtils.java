package com.github.sommeri.less4j.utils;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;

public class ArraysUtils {

  public static <T> List<T> asNonNullList(T... a) {
    List<T> result = new ArrayList<T>();
    for (T t : a) {
      if (t != null)
        result.add(t);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  public static <T extends ASTCssNode> List<T> deeplyClonedList(List<T> list) {
    List<T> result = new ArrayList<T>();
    for (T t : list) {
      result.add((T)t.clone());
    }
    return result;
  }

}
