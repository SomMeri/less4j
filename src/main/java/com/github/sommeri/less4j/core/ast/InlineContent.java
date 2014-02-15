package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class InlineContent extends ASTCssNode {

  private String value;

  public InlineContent(HiddenTokenAwareTree token, String value) {
    super(token);
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.INLINE_CONTENT;
  }

  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

  @Override
  public String toString() {
    return "" + value;
  }

  @Override
  public InlineContent clone() {
    return (InlineContent) super.clone();
  }
}
