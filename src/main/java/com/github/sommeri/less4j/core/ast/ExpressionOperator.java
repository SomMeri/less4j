package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

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

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

  public enum Operator {
    SOLIDUS("/"), COMMA(","), STAR("*"), EMPTY_OPERATOR("' '"), MINUS("-"), PLUS("+");
    
    private final String symbol;

    private Operator(String symbol) {
      this.symbol = symbol;
    }

    public String getSymbol() {
      return symbol;
    }
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.EXPRESSION_OPERATOR;
  }

  @Override
  public String toString() {
    return "" + operator.getSymbol();
  }
  
  @Override
  public ExpressionOperator clone() {
    return (ExpressionOperator) super.clone();
  }
}
