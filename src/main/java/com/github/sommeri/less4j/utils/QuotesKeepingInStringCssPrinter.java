package com.github.sommeri.less4j.utils;

import java.util.List;

import com.github.sommeri.less4j.core.ast.ColorExpression;
import com.github.sommeri.less4j.core.ast.Comment;

public class QuotesKeepingInStringCssPrinter extends CssPrinter {

  @Override
  protected void appendComments(List<Comment> comments, boolean ensureSeparator) {
  }

  @Override
  protected boolean appendColorExpression(ColorExpression expression) {
    builder.append(expression.getValue());
    return true;
  }
}
