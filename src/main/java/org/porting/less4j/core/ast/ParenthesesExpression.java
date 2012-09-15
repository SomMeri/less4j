package org.porting.less4j.core.ast;

import java.util.List;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;
import org.porting.less4j.utils.ArraysUtils;

public class ParenthesesExpression extends Expression {

  private Expression enclosedExpression;

  public ParenthesesExpression(HiddenTokenAwareTree token, Expression enclosedExpression) {
    super(token);
    this.enclosedExpression = enclosedExpression;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(enclosedExpression);
  }

  public Expression getEnclosedExpression() {
    return enclosedExpression;
  }

  public void setEnclosedExpression(Expression enclosedExpression) {
    this.enclosedExpression = enclosedExpression;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.PARENTHESES_EXPRESSION;
  }

  @Override
  public String toString() {
    return "(" +enclosedExpression+ ")";
  }

}
