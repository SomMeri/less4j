package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class EscapedSelector extends SelectorPart {

  private String value;
  private String quoteType;

  public EscapedSelector(HiddenTokenAwareTree underlyingStructure, String value, String quoteTypes, SelectorCombinator leadingCombinator) {
    super(underlyingStructure, leadingCombinator);
    this.value = value;
    this.quoteType = quoteTypes;
  }

  public String getQuoteType() {
    return quoteType;
  }

  public void setQuoteTypes(String quoteTypes) {
    this.quoteType = quoteTypes;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }

  @Override
  public EscapedSelector clone() {
    return (EscapedSelector)super.clone();
  }


  @Override
  public List<ASTCssNode> getChilds() {
    return super.getChilds();
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.ESCAPED_SELECTOR;
  }

}
