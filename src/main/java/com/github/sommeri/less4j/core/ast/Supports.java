package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class Supports extends Directive {

  private String dialect;
  private SupportsCondition condition;
  private GeneralBody body;

  public Supports(HiddenTokenAwareTree token, String dialect) {
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

  public SupportsCondition getCondition() {
    return condition;
  }

  public void setCondition(SupportsCondition condition) {
    this.condition = condition;
  }

  @Override
  @NotAstProperty
  public List<ASTCssNode> getChilds() {
    List<ASTCssNode> result = ArraysUtils.asNonNullList(condition, body);
    return result;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.SUPPORTS;
  }

  @Override
  public Supports clone() {
    Supports result = (Supports) super.clone();
    result.condition = condition==null? null : condition.clone();
    result.body = body==null? null : body.clone();
    result.configureParentToAllChilds();
    return result;
  }

}
