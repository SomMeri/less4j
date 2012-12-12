package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class EscapedValue extends Expression {

  private String value;

  public EscapedValue(HiddenTokenAwareTree token, String value) {
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
    return ASTCssNodeType.ESCAPED_VALUE;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

  @Override
  public String toString() {
    return value;
  }

  @Override
  public EscapedValue clone() {
    return (EscapedValue) super.clone();
  }
}
