package com.github.sommeri.less4j.utils;

import java.util.List;

import com.github.sommeri.less4j.core.ast.ColorExpression;
import com.github.sommeri.less4j.core.ast.Comment;
import com.github.sommeri.less4j.core.ast.CssString;

public class InStringCssPrinter extends CssPrinter {

  @Override
  protected void appendComments(List<Comment> comments, boolean ensureSeparator) {
  }

  @Override
  public boolean appendCssString(CssString expression) {
    builder.append(expression.getValue());
    return true;
  }

  @Override
  protected boolean appendColorExpression(ColorExpression expression) {
    builder.append(expression.getValueInHexadecimal());
    return true;
  }
}
