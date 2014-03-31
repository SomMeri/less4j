package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class ListExpressionOperator extends ASTCssNode {

  private Operator operator;
  
  public ListExpressionOperator(HiddenTokenAwareTree underlyingStructure) {
    this(underlyingStructure, Operator.EMPTY_OPERATOR);
  }

  public ListExpressionOperator(HiddenTokenAwareTree underlyingStructure, Operator operator) {
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
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

  public enum Operator {
    COMMA(","), EMPTY_OPERATOR("' '");
    
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
    return ASTCssNodeType.LIST_EXPRESSION_OPERATOR;
  }

  @Override
  public String toString() {
    return "" + operator.getSymbol();
  }
  
  @Override
  public ListExpressionOperator clone() {
    return (ListExpressionOperator) super.clone();
  }
}
