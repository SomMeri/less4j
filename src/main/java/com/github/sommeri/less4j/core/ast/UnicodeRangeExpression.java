package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class UnicodeRangeExpression extends Expression {
  
  private String value;

  public UnicodeRangeExpression(HiddenTokenAwareTree underlyingStructure) {
    this(underlyingStructure, null);
  }

  public UnicodeRangeExpression(HiddenTokenAwareTree underlyingStructure, String value) {
    super(underlyingStructure);
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  @NotAstProperty
  public List<ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.UNICODE_RANGE_EXPRESSION;
  }

  @Override
  public String toString() {
    return "" + value;
  }

  @Override
  public UnicodeRangeExpression clone() {
    return (UnicodeRangeExpression) super.clone();
  }

}
