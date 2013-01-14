package com.github.sommeri.less4j.core.parser;

import com.github.sommeri.less4j.core.ast.SelectorCombinator;
import com.github.sommeri.less4j.core.ast.SelectorCombinator.Combinator;
import com.github.sommeri.less4j.utils.PrintUtils;

public class ConversionUtils {

  public static SelectorCombinator createSelectorCombinator(HiddenTokenAwareTree token) {
    Combinator combinator = safeToSelectorCombinator(token);
    if (combinator == null)
      throw new IllegalStateException("Unknown: " + token.getType() + " " + PrintUtils.toName(token.getType()));

    return new SelectorCombinator(token, combinator);
  }

  public static boolean isSelectorCombinator(HiddenTokenAwareTree token) {
    return null != safeToSelectorCombinator(token);
  }

  private static SelectorCombinator.Combinator safeToSelectorCombinator(HiddenTokenAwareTree token) {
    switch (token.getType()) {
    case LessLexer.PLUS:
      return SelectorCombinator.Combinator.ADJACENT_SIBLING;
    case LessLexer.GREATER:
      return SelectorCombinator.Combinator.CHILD;
    case LessLexer.TILDE:
      return SelectorCombinator.Combinator.GENERAL_SIBLING;
    case LessLexer.EMPTY_COMBINATOR:
      return SelectorCombinator.Combinator.DESCENDANT;
    default:
      return null;
    }
  }

}
