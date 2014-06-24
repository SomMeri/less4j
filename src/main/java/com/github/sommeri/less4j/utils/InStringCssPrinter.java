package com.github.sommeri.less4j.utils;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ColorExpression;
import com.github.sommeri.less4j.core.ast.Comment;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.DetachedRuleset;

public class InStringCssPrinter extends CssPrinter {

  private List<ASTCssNode> unprintableNodes = new ArrayList<ASTCssNode>();

  public InStringCssPrinter() {
  }

  @Override
  protected void appendComments(List<Comment> comments, boolean ensureSeparator) {
  }

  @Override
  public boolean appendCssString(CssString expression) {
    cssOnly.append(expression.getValue());
    return true;
  }

  @Override
  protected boolean appendColorExpression(ColorExpression expression) {
    cssOnly.append(expression.getValueInHexadecimal());
    return true;
  }
  
  public boolean appendDetachedRuleset(DetachedRuleset node) {
    cssOnly.append(ERROR);
    unprintableNodes.add(node);
    return true;
  }

  public List<ASTCssNode> getUnprintableNodes() {
    return unprintableNodes;
  }

}
