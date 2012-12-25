package com.github.sommeri.less4j.core.compiler.expressions.strings;

import java.util.regex.Pattern;

import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionEvaluator;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.InStringCssPrinter;

public class StringInterpolator extends AbstractStringReplacer<ExpressionEvaluator> {

  private static final Pattern STR_INTERPOLATION = Pattern.compile("@\\{([^\\{\\}@])*\\}");

  @Override
  protected Pattern getPattern() {
    return STR_INTERPOLATION;
  }

  @Override
  protected String extractMatchName(String group) {
    return "@"+group.substring(2, group.length()-1);
  }

  @Override
  protected String replacementValue(ExpressionEvaluator expressionEvaluator, HiddenTokenAwareTree technicalUnderlying, MatchRange matchRange) {
    Expression value = expressionEvaluator.evaluateIfPresent(new Variable(technicalUnderlying, matchRange.getName()));
    if (value!=null && (value instanceof CssString)) {
      CssString string = (CssString) value;
      return replaceIn(string.getValue(), expressionEvaluator, technicalUnderlying);
    } else if (value==null) {
      return matchRange.getFullMatch();
    } else {
      InStringCssPrinter builder = new InStringCssPrinter();
      builder.append(value);
      String replacement = builder.toString();
      return replacement;
    }
  }

}
