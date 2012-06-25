package org.porting.less4j.core.ast;

import org.antlr.runtime.tree.CommonTree;

//FIXME: make expression abstract and then make both expressions and terms its childs. This way the composed expresison 
//will be expression operator expression and not something operator something else. 
public class ComposedExpression extends Expression {

  private Expression left;
  private Operator operator;
  private Expression right;

  public ComposedExpression(CommonTree token, Expression left, Operator operator, Expression right) {
    super(token);
    //FIXME: remove later on to be able to handle malformed trees, usefull for debug now
    assert (left==null || operator ==null || right==null);
    
    this.left = left;
    this.operator = operator;
    this.right = right;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.COMPOSED_EXPRESSION;
  }
  
  public Operator getOperator() {
    return operator;
  }

  public void setOperator(Operator operator) {
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

  public enum Operator {
    SOLIDUS, COMMA, STAR, EMPTY_OPERATOR;
  }

}
