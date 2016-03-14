package com.github.sommeri.less4j.utils;

public class MathUtils {

  public static boolean equals(Double value, Double value2) {
    if (value == null || value2==null)
      return value2 == null && value==null;

    value = normalize0(value);
    value2 = normalize0(value2);
    return value.compareTo(value2) == 0;
  }

  private static Double normalize0(Double value) {
    if (value.compareTo(-0.0) == 0)
      value = 0.0;
    return value;
  }
  

}
