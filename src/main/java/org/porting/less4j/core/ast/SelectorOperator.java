package org.porting.less4j.core.ast;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

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

  public enum Operator {
    NONE, EQUALS, INCLUDES, SPECIAL_PREFIX, PREFIXMATCH, SUFFIXMATCH, SUBSTRINGMATCH
  }
}
