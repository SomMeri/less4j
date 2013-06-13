package com.github.sommeri.less4j.core.ast;

import java.util.LinkedList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ExpressionOperator.Operator;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class ComposedExpression extends Expression {

  private Expression left;
  private ExpressionOperator operator;
  private Expression right;

  public ComposedExpression(HiddenTokenAwareTree token, Expression left, ExpressionOperator operator, Expression right) {
    super(token);
    this.left = left;
    this.operator = operator;
    this.right = right;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.COMPOSED_EXPRESSION;
  }
  
  public ExpressionOperator getOperator() {
    return operator;
  }

  public void setOperator(ExpressionOperator operator) {
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
  public List<Expression> splitByComma() {
    return doSplitByComma();
  }

  private LinkedList<Expression> doSplitByComma() {
    LinkedList<Expression> result = new LinkedList<Expression>();
    if (operator.getOperator()!=Operator.COMMA && operator.getOperator()!=Operator.EMPTY_OPERATOR) {
      result.add(this);
      return result;
    }

    LinkedList<Expression> left = splitByComma(getLeft());
    LinkedList<Expression> right = splitByComma(getRight());
    
    if (operator.getOperator()!=Operator.EMPTY_OPERATOR) {
      result.addAll(left);
      result.addAll(right);
      return result;
    }
    
    Expression lastLeft = left.pollLast();
    Expression firstRight = right.pollFirst();
    result.addAll(left);
    result.add(new ComposedExpression(lastLeft.getUnderlyingStructure(), lastLeft, operator, firstRight));
    result.addAll(right);

    return result;
  }

  private LinkedList<Expression> splitByComma(Expression expression) {
    if (expression.getType()==ASTCssNodeType.COMPOSED_EXPRESSION) {
      ComposedExpression composed = (ComposedExpression) expression;
      return composed.doSplitByComma();
    } else {
      LinkedList<Expression> result = new LinkedList<Expression>();
      result.add(expression);
      return result;
    }
  }
  
  @Override
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(left, operator, right);
  }

  @Override
  public String toString() {
    return "[" + left + operator + right + "]";
  }

  @Override
  public ComposedExpression clone() {
    ComposedExpression result = (ComposedExpression) super.clone();
    result.left = left==null?null:left.clone();
    result.operator = operator==null?null:operator.clone();
    result.right = right==null?null:right.clone();
    result.configureParentToAllChilds();
    return result;
  }
}
