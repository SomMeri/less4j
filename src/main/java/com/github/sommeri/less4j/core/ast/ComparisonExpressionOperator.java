package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class ComparisonExpressionOperator extends ASTCssNode {

  private Operator operator;
  
  public ComparisonExpressionOperator(HiddenTokenAwareTree underlyingStructure, Operator operator) {
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
    GREATER, GREATER_OR_EQUAL, OPEQ, LOWER_OR_EQUAL, LOWER;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.EXPRESSION_OPERATOR;
  }

  @Override
  public String toString() {
    return "" + operator;
  }
  
  @Override
  public ComparisonExpressionOperator clone() {
    return (ComparisonExpressionOperator) super.clone();
  }
}
