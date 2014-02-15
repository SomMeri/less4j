package com.github.sommeri.less4j.core.parser;

import com.github.sommeri.less4j.core.ast.ColorExpression;
import com.github.sommeri.less4j.core.ast.ColorExpression.ColorWithAlphaExpression;
import com.github.sommeri.less4j.core.ast.NamedColorExpression;
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
    case LessLexer.HAT:
      return SelectorCombinator.Combinator.HAT;
    case LessLexer.CAT:
      return SelectorCombinator.Combinator.CAT;
    default:
      return null;
    }
  }

  public static ColorExpression parseColor(HiddenTokenAwareTree token, String string) {
    if (string==null)
      return null;
    
    if (NamedColorExpression.isColorName(string))
      return new NamedColorExpression(token, string);

    double red = decodeColorPart(string, 0);
    double green = decodeColorPart(string, 1);
    double blue = decodeColorPart(string, 2);
    
    if (Double.isNaN(red) || Double.isNaN(green) || Double.isNaN(blue))
      return null;
    
    double alpha = decodeColorPart(string, 3);
    if (!Double.isNaN(alpha))
      return new ColorWithAlphaExpression(token, string, red, green, blue, alpha);

    return new ColorExpression(token, string, red, green, blue);
  }

  private static double decodeColorPart(String color, int i) {
    try {
      if (color.length() < 7) {
        String substring = color.substring(i + 1, i + 2);
        return Integer.parseInt(substring + substring, 16);
      }

      return Integer.parseInt(color.substring(i * 2 + 1, i * 2 + 3), 16);
    } catch (RuntimeException ex) {
      return Double.NaN;
    }
  }

}
