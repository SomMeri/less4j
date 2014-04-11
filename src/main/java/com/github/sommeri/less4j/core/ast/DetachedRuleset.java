package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class DetachedRuleset extends Expression implements BodyOwner<GeneralBody> {

  private GeneralBody body;

  public DetachedRuleset(HiddenTokenAwareTree token, GeneralBody body) {
    super(token);
    this.body = body;
  }
  
  public GeneralBody getBody() {
    return body;
  }

  public void setBody(GeneralBody body) {
    this.body = body;
  }

  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(body);
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.DETACHED_RULESET;
  }

  @Override
  public DetachedRuleset clone() {
    DetachedRuleset result = (DetachedRuleset) super.clone();
    result.body = body==null?null:body.clone();
    result.configureParentToAllChilds();
    return result;
  }

}
