package org.porting.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

public class CssString extends Expression {

  private String quoteType;
  private String value;

  public CssString(HiddenTokenAwareTree token, String value, String quoteType) {
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
    return ASTCssNodeType.STRING_EXPRESSION;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

  @Override
  public String toString() {
    return "" + quoteType + value + quoteType;
  }

}
