package org.porting.less4j.core.ast;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

public class ExpressionOperator extends ASTCssNode {

  private Operator operator;
  
  public ExpressionOperator(HiddenTokenAwareTree underlyingStructure) {
    this(underlyingStructure, Operator.EMPTY_OPERATOR);
  }

  public ExpressionOperator(HiddenTokenAwareTree underlyingStructure, Operator operator) {
    super(underlyingStructure);
    this.operator = operator;
  }

  public Operator getOperator() {
    return operator;
  }

  public void setOperator(Operator operator) {
    this.operator = operator;
  }


  public enum Operator {
    SOLIDUS, COMMA, STAR, EMPTY_OPERATOR;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.EXPRESSION_OPERATOR;
  }
}
