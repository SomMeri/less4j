package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class EscapedSelector extends SelectorPart {

  private String value;

  public EscapedSelector(HiddenTokenAwareTree underlyingStructure, String value) {
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
  public String toString() {
    return value;
  }

  @Override
  public EscapedSelector clone() {
    return (EscapedSelector)super.clone();
  }


  @Override
  public List<? extends ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.ESCAPED_SELECTOR;
  }

}
