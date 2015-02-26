package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class EscapedValue extends Expression {

  private String value;
  private String quoteType; //"\""

  public EscapedValue(HiddenTokenAwareTree token, String value, String quoteType) {
    super(token);
    this.value = value;
    this.quoteType = quoteType;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getQuoteType() {
    return quoteType;
  }

  public void setQuoteType(String quoteType) {
    this.quoteType = quoteType;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.ESCAPED_VALUE;
  }

  @Override
  @NotAstProperty
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
