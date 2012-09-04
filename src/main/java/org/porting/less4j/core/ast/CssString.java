package org.porting.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

public class CssString extends Expression {

  private String value;

  public CssString(HiddenTokenAwareTree token, String value) {
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
    return ASTCssNodeType.STRING_EXPRESSION;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

}
