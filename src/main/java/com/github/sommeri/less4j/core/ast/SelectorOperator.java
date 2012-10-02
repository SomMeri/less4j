package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class SelectorOperator extends ASTCssNode {

  private Operator operator;
  
  public SelectorOperator(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  public SelectorOperator(HiddenTokenAwareTree underlyingStructure, Operator operator) {
    this(underlyingStructure);
    this.operator = operator;
  }
  
  public Operator getOperator() {
    return operator;
  }

  public void setOperator(Operator operator) {
    this.operator = operator;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.SELECTOR_OPERATOR;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

  public enum Operator {
    NONE, EQUALS, INCLUDES, SPECIAL_PREFIX, PREFIXMATCH, SUFFIXMATCH, SUBSTRINGMATCH
  }
}
