package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class Viewport extends ASTCssNode {

  //I have to do this because of a comment in following less: `@viewport /*comment */ { ... }`
  private ViewportBody body;

  public Viewport(HiddenTokenAwareTree token) {
    super(token);
  }

  public ViewportBody getBody() {
    return body;
  }

  public void setBody(ViewportBody body) {
    this.body = body;
  }

  @Override
  public List<ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList((ASTCssNode)body);
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.VIEWPORT;
  }

  @Override
  public Viewport clone() {
    Viewport result = (Viewport) super.clone();
    result.body = body==null? null : body.clone();
    result.configureParentToAllChilds();
    return result;
  }

}
