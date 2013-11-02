package com.github.sommeri.less4j.core.compiler.selectors;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.ElementSubsequent;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.ast.PseudoClass;
import com.github.sommeri.less4j.core.ast.SelectorAttribute;
import com.github.sommeri.less4j.core.problems.BugHappened;
import com.github.sommeri.less4j.utils.ListsComparator.ListMemberComparator;

public class ElementSubsequentComparator implements ListMemberComparator<ElementSubsequent> {

  private final SelectorsComparatorUtils utils;
  private final GeneralComparatorForExtend generalComparator;

  public ElementSubsequentComparator(GeneralComparatorForExtend generalComparator, SelectorsComparatorUtils utils) {
    this.utils = utils;
    this.generalComparator = generalComparator;
  }

  @Override
  public boolean equals(ElementSubsequent first, ElementSubsequent second) {
      if (first.getType() != second.getType()) {
        return false;
      }

      switch (first.getType()) {
      case CSS_CLASS:
      case ID_SELECTOR:
      case PSEUDO_ELEMENT: {
        return utils.nullSafeEquals(first.getFullName(), second.getFullName());
      }

      case PSEUDO_CLASS: {
        PseudoClass pClass1 = (PseudoClass) first;
        PseudoClass pClass2 = (PseudoClass) second;

        return pseudoclassesEqual(pClass1, pClass2);
      }

      case SELECTOR_ATTRIBUTE: {
        SelectorAttribute attribute1 = (SelectorAttribute) first;
        SelectorAttribute attribute2 = (SelectorAttribute) second;

        return attributesEqual(attribute1, attribute2);
      }

      default:
        throw new BugHappened("Unexpected subsequent type: " + first.getType(), first);
      }
  }

  @Override
  public boolean prefix(ElementSubsequent lookFor, ElementSubsequent inside) {
    return equals(lookFor, inside);
  }

  @Override
  public boolean suffix(ElementSubsequent lookFor, ElementSubsequent inside) {
    return equals(lookFor, inside);
  }
  
  private boolean attributesEqual(SelectorAttribute att1, SelectorAttribute att2) {
    if (!utils.nullSafeEquals(att1.getFullName(), att2.getFullName()))
      return false;

    if (!utils.selectorOperatorsEquals(att1.getOperator(), att2.getOperator()))
      return false;

    Expression v1 = att1.getValue();
    Expression v2 = att2.getValue();

    if (v1 == null || v2 == null)
      return v1 == null && v2 == null;

    //extend keyword is "smart". It knows that [attribute=something] is the same as [attribute='something'] and the same [attribute="something"]
    String smart1 = toSmartAttributeValue(v1);
    String smart2 = toSmartAttributeValue(v2);

    if (smart1 != null && smart2 != null)
      return utils.nullSafeEquals(smart1, smart2);

    return generalComparator.equals(v1, v2);
  }

  private String toSmartAttributeValue(Expression value) {
    switch (value.getType()) {
    case IDENTIFIER_EXPRESSION:
      return ((IdentifierExpression) value).getValue();
    case STRING_EXPRESSION:
      return ((CssString) value).getValue();
    default:
      return null;
    }
  }

  private boolean pseudoclassesEqual(PseudoClass pClass1, PseudoClass pClass2) {
    if (!utils.nullSafeEquals(pClass1.getFullName(), pClass2.getFullName()))
      return false;

    ASTCssNode parameter1 = pClass1.getParameter();
    ASTCssNode parameter2 = pClass2.getParameter();

    if (parameter1 == null && parameter2 == null)
      return true;

    return generalComparator.equals(parameter1, parameter2);
  }

  @Override
  public boolean contains(ElementSubsequent lookFor, ElementSubsequent inside) {
    return equals(lookFor, inside);
  }

}