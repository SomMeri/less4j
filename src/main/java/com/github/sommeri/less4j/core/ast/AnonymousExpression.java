package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class AnonymousExpression extends Expression {

  private String value;
  
  public AnonymousExpression(HiddenTokenAwareTree token, String value) {
    super(token);
    this.value = value;
  }
  
  public String getValue() {
    return value;
  }

  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.ANONYMOUS;
  }
  
  @Override
  public AnonymousExpression clone() {
    AnonymousExpression result = (AnonymousExpression) super.clone();
    return result;
  }

}
