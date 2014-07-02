package com.github.sommeri.less4j.core.compiler.expressions.strings;

import java.util.regex.Pattern;

import com.github.sommeri.less4j.EmbeddedScriptGenerator;
import com.github.sommeri.less4j.EmbeddedLessGenerator;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionEvaluator;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.utils.CssPrinter;

public class StringInterpolator extends AbstractStringReplacer<ExpressionEvaluator> {

  private static final Pattern STR_INTERPOLATION = Pattern.compile("@\\{([^\\{\\}@])*\\}");
  private final EmbeddedScriptGenerator embeddedScriptEvaluator;
  private final ProblemsHandler problemsHandler;

  public StringInterpolator(ProblemsHandler problemsHandler) {
    this(new EmbeddedLessGenerator(), problemsHandler);
  }

  public StringInterpolator(EmbeddedScriptGenerator embeddedScriptEvaluator, ProblemsHandler problemsHandler) {
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

    String result = embeddedScriptEvaluator.toScript(value, problemsHandler);
    if (result==null) {
      problemsHandler.stringInterpolationNotSupported(technicalUnderlying, value);
      result = CssPrinter.ERROR;
    }
    return result;
  }

}
