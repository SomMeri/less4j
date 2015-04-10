package com.github.sommeri.less4j.core.parser;

import com.github.sommeri.less4j.core.ast.ColorExpression;
import com.github.sommeri.less4j.core.ast.ColorExpression.ColorWithAlphaExpression;
import com.github.sommeri.less4j.core.ast.NamedColorExpression;
import com.github.sommeri.less4j.core.ast.SelectorCombinator;
import com.github.sommeri.less4j.core.ast.SelectorCombinator.CombinatorType;
import com.github.sommeri.less4j.utils.PrintUtils;

public class ConversionUtils {

  public static SelectorCombinator createSelectorCombinator(HiddenTokenAwareTree token) {
    CombinatorType combinator = safeToSelectorCombinator(token);
    if (combinator == null)
      throw new IllegalStateException("Unknown: " + token.getGeneralType() + " " + PrintUtils.toName(token.getGeneralType()));

    String symbol = extractSymbol(token, combinator);
    return new SelectorCombinator(token, combinator, symbol);
  }

  private static String extractSymbol(HiddenTokenAwareTree token, CombinatorType combinator) {
    if (combinator != CombinatorType.NAMED)
      return token.getText();
    
    StringBuilder symbol = new StringBuilder();
    for (HiddenTokenAwareTree child : token.getChildren()) {
      symbol.append(child.getText());
    }
    
    return symbol.toString();
  }

  public static boolean isSelectorCombinator(HiddenTokenAwareTree token) {
    return null != safeToSelectorCombinator(token);
  }

  private static SelectorCombinator.CombinatorType safeToSelectorCombinator(HiddenTokenAwareTree token) {
    switch (token.getGeneralType()) {
    case LessLexer.PLUS:
      return SelectorCombinator.CombinatorType.ADJACENT_SIBLING;
    case LessLexer.GREATER:
      return SelectorCombinator.CombinatorType.CHILD;
    case LessLexer.TILDE:
      return SelectorCombinator.CombinatorType.GENERAL_SIBLING;
    case LessLexer.EMPTY_COMBINATOR:
      return SelectorCombinator.CombinatorType.DESCENDANT;
    case LessLexer.HAT:
      return SelectorCombinator.CombinatorType.HAT;
    case LessLexer.CAT:
      return SelectorCombinator.CombinatorType.CAT;
    case LessLexer.NAMED_COMBINATOR:
      return SelectorCombinator.CombinatorType.NAMED;
    default:
      return null;
    }
  }

  public static ColorExpression parseColor(HiddenTokenAwareTree token, String string) {
    if (string == null)
      return null;

    if (NamedColorExpression.isColorName(string))
      return NamedColorExpression.createNamedColorExpression(token, string);

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
