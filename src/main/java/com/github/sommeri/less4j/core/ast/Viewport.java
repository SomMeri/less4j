package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class Viewport extends ASTCssNode implements BodyOwner<GeneralBody> {

  //I have to do this because of a comment in following less: `@viewport /*comment */ { ... }`
  private GeneralBody body;
  private String dialect;

  public Viewport(HiddenTokenAwareTree token, String dialect) {
    super(token);
    this.dialect = dialect;
  }

  public GeneralBody getBody() {
    return body;
  }

  public void setBody(GeneralBody body) {
    this.body = body;
  }

  public String getDialect() {
    return dialect;
  }

  public void setDialect(String dialect) {
    this.dialect = dialect;
  }

  @Override
  @NotAstProperty
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
