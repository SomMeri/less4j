package org.porting.less4j.core.parser;

import org.porting.less4j.core.ast.SelectorCombinator;

public class ConversionUtils {
  
  public static SelectorCombinator createSelectorCombinator(HiddenTokenAwareTree token) {
    return new SelectorCombinator(token, toSelectorCombinator(token));
  }
  
  public static SelectorCombinator.Combinator toSelectorCombinator(HiddenTokenAwareTree token) {
    switch (token.getType()) {
    case LessLexer.PLUS:
      return SelectorCombinator.Combinator.ADJACENT_SIBLING;
    case LessLexer.GREATER:
      return SelectorCombinator.Combinator.CHILD;
    case LessLexer.TILDE:
      return SelectorCombinator.Combinator.GENERAL_SIBLING;
    case LessLexer.EMPTY_COMBINATOR:
      return SelectorCombinator.Combinator.DESCENDANT;
    }

    throw new IllegalStateException("Unknown: " + token.getType());
  }

}
