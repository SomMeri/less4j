package com.github.sommeri.less4j.core.compiler.selectors;

import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.ElementSubsequent;
import com.github.sommeri.less4j.core.ast.SelectorPart;
import com.github.sommeri.less4j.core.ast.SimpleSelector;
import com.github.sommeri.less4j.core.problems.BugHappened;
import com.github.sommeri.less4j.utils.ListsComparator.ListMemberComparator;

public class SelectorPartComparator implements ListMemberComparator<SelectorPart> {

  private final SimpleSelectorComparator simpleSelectorComparator;

  public SelectorPartComparator(SimpleSelectorComparator simpleSelectorComparator) {
    this.simpleSelectorComparator = simpleSelectorComparator;
  }

  @Override
  public boolean equals(SelectorPart first, SelectorPart second) {
    if (!prefix(first, second))
      return false;

    //this assumes that isSelectorPartPrefix thrown exception on anything that is not simple selector
    List<ElementSubsequent> firstSubsequent = ((SimpleSelector) first).getSubsequent();
    List<ElementSubsequent> secondSubsequent = ((SimpleSelector) second).getSubsequent();

    return firstSubsequent.size() == secondSubsequent.size();
  }

  @Override
  public boolean prefix(SelectorPart lookFor, SelectorPart inside) {
    validateSimpleSelector(lookFor);
    validateSimpleSelector(inside);

    return simpleSelectorComparator.prefix((SimpleSelector) lookFor, (SimpleSelector) inside);
  }

  private void validateSimpleSelector(SelectorPart selector) {
    if (selector.getType() != ASTCssNodeType.SIMPLE_SELECTOR) {
      throw new BugHappened("Unexpected selector part type " + selector.getType() + ". Anything but simple selector should have been removed from tree. ", selector);
    }
  }

  @Override
  public boolean suffix(SelectorPart lookFor, SelectorPart inside) {
    validateSimpleSelector(lookFor);
    validateSimpleSelector(inside);

    return simpleSelectorComparator.suffix((SimpleSelector) lookFor, (SimpleSelector) inside);
  }

  public boolean contains(SelectorPart lookFor, SelectorPart inside) {
    validateSimpleSelector(lookFor);
    validateSimpleSelector(inside);

    return simpleSelectorComparator.contains((SimpleSelector) lookFor, (SimpleSelector) inside);
  }

}
