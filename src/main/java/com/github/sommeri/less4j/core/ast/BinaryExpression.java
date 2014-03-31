package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class BinaryExpression extends Expression {

  private Expression left;
  private BinaryExpressionOperator operator;
  private Expression right;

  public BinaryExpression(HiddenTokenAwareTree token, Expression left, BinaryExpressionOperator operator, Expression right) {
    super(token);
    this.left = left;
    this.operator = operator;
    this.right = right;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.BINARY_EXPRESSION;
  }
  
  public BinaryExpressionOperator getOperator() {
    return operator;
  }

  public void setOperator(BinaryExpressionOperator operator) {
    this.operator = operator;
  }
 
  public Expression getLeft() {
    return left;
  }

  public void setLeft(Expression left) {
    this.left = left;
  }

  public Expression getRight() {
    return right;
  }

  public void setRight(Expression right) {
    this.right = right;
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
  public BinaryExpression clone() {
    BinaryExpression result = (BinaryExpression) super.clone();
    result.left = left==null?null:left.clone();
    result.operator = operator==null?null:operator.clone();
    result.right = right==null?null:right.clone();
    result.configureParentToAllChilds();
    return result;
  }
}
