package com.github.sommeri.less4j.core.compiler.expressions.strings;

import java.util.regex.Pattern;

import com.github.sommeri.less4j.EmbeddedScripting;
import com.github.sommeri.less4j.LessStringsEvaluator;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionEvaluator;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class StringInterpolator extends AbstractStringReplacer<ExpressionEvaluator> {

  private static final Pattern STR_INTERPOLATION = Pattern.compile("@\\{([^\\{\\}@])*\\}");
  private final EmbeddedScripting embeddedScriptEvaluator;
  private final ProblemsHandler problemsHandler;

  public StringInterpolator(ProblemsHandler problemsHandler) {
    this(new LessStringsEvaluator(), problemsHandler);
  }

  public StringInterpolator(EmbeddedScripting embeddedScriptEvaluator, ProblemsHandler problemsHandler) {
    this.embeddedScriptEvaluator = embeddedScriptEvaluator;
    this.problemsHandler = problemsHandler;
  }

  @Override
  protected Pattern getPattern() {
    return STR_INTERPOLATION;
  }

  @Override
  protected String extractMatchName(String group) {
    return "@" + group.substring(2, group.length() - 1);
  }

  @Override
  protected String replacementValue(ExpressionEvaluator expressionEvaluator, HiddenTokenAwareTree technicalUnderlying, MatchRange matchRange) {
    Expression value = expressionEvaluator.evaluateIfPresent(new Variable(technicalUnderlying, matchRange.getName()));
    if (value == null) {
      return matchRange.getFullMatch();
    }

    return embeddedScriptEvaluator.toScriptExpression(value, problemsHandler);
  }

}
