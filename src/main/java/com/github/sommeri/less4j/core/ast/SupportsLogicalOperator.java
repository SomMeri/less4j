package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class SupportsLogicalOperator extends ASTCssNode implements Cloneable {

  private Operator operator;

  public SupportsLogicalOperator(HiddenTokenAwareTree underlyingStructure, Operator operator) {
    super(underlyingStructure);
    this.operator = operator;
  }

  public Operator getOperator() {
    return operator;
  }

  public void setOperator(Operator operator) {
    this.operator = operator;
  }

  public boolean isFaulty() {
    return operator==null;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.SUPPORTS_LOGICAL_OPERATOR;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

  public enum Operator {
    AND("and"), OR("or");
    
    private final String symbol;

    private Operator(String symbol) {
      this.symbol = symbol;
    }

    public String getSymbol() {
      return symbol;
    }

    public static Map<String, Operator> getSymbolsMap() {
      Map<String, Operator> result = new HashMap<String, Operator>();
      for (Operator operator : values()) {
        result.put(operator.getSymbol(), operator);
      }
      return result;
    }
  }

  @Override
  public SupportsLogicalOperator clone() {
    return (SupportsLogicalOperator) super.clone();
  }

  @Override
  public String toString() {
    return "" + operator.getSymbol();
  }
  
}
