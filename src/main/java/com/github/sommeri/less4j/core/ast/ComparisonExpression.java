package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class ComparisonExpression extends Expression {
  
  private Expression left;
  private ComparisonExpressionOperator operator;
  private Expression right;

  public ComparisonExpression(HiddenTokenAwareTree underlyingStructure) {
   super(underlyingStructure);
  }

  public ComparisonExpression(HiddenTokenAwareTree underlyingStructure, Expression left, ComparisonExpressionOperator operator, Expression right) {
    super(underlyingStructure);
    this.left = left;
    this.operator = operator;
    this.right = right;
  }

  public Expression getLeft() {
    return left;
  }

  public void setLeft(Expression left) {
    this.left = left;
  }

  public ComparisonExpressionOperator getOperator() {
    return operator;
  }

  public void setOperator(ComparisonExpressionOperator operator) {
    this.operator = operator;
  }

  public Expression getRight() {
    return right;
  }

  public void setRight(Expression right) {
    this.right = right;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.COMPARISON_EXPRESSION;
  }

  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(left, operator, right);
  }

  @Override
  public String toString() {
    return "[" + left + operator + right + "]";
  }

  @Override
  public ComparisonExpression clone() {
    ComparisonExpression result = (ComparisonExpression) super.clone();
    result.left = left==null?null:left.clone();
    result.operator = operator==null?null:operator.clone();
    result.right = right==null?null:right.clone();
    result.configureParentToAllChilds();
    return result;
  }

}
