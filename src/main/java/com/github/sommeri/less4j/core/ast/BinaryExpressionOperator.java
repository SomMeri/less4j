package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class BinaryExpressionOperator extends ASTCssNode {

  private Operator operator;
  
  public BinaryExpressionOperator(HiddenTokenAwareTree underlyingStructure, Operator operator) {
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
    SOLIDUS("/"), STAR("*"), MINUS("-"), PLUS("+");
    
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
    return ASTCssNodeType.BINARY_EXPRESSION_OPERATOR;
  }

  @Override
  public String toString() {
    return "" + operator.getSymbol();
  }
  
  @Override
  public BinaryExpressionOperator clone() {
    return (BinaryExpressionOperator) super.clone();
  }
}
