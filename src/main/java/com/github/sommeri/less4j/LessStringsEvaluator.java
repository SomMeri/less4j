package com.github.sommeri.less4j;

import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.utils.InStringCssPrinter;

public class LessStringsEvaluator implements EmbeddedScripting {

  public LessStringsEvaluator() {
    super();
  }

  @Override
  public String toScriptExpression(Expression value, LessProblems problemsHandler) {
    InStringCssPrinter builder = new InStringCssPrinter();
    builder.append(value);
    String replacement = builder.toString();
    return replacement;
  }

}
