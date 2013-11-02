package com.github.sommeri.less4j.core.compiler.selectors;

import com.github.sommeri.less4j.core.ast.InterpolableName;
import com.github.sommeri.less4j.core.ast.SelectorOperator;

public class SelectorsComparatorUtils {

  public boolean selectorOperatorsEquals(SelectorOperator operator1, SelectorOperator operator2) {
    if (operator1 == null)
      return operator2 == null;

    return operator1.getOperator() == operator2.getOperator();
  }

  public boolean interpolableNamesEqual(InterpolableName firstName, InterpolableName secondName) {
    if (firstName == null)
      return secondName == null;

    if (secondName == null)
      return false;

    return nullSafeEquals(firstName.getName(), secondName.getName());
  }

  public boolean nullSafeEquals(Object s1, Object s2) {
    if (s1 == null || s2 == null)
      return s1 == null && s2 == null;

    return s1.equals(s2);
  }

}
