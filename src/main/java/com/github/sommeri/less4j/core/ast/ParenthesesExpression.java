package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class ParenthesesExpression extends Expression {

  private Expression enclosedExpression;

  public ParenthesesExpression(HiddenTokenAwareTree token, Expression enclosedExpression) {
    super(token);
    this.enclosedExpression = enclosedExpression;
  }

  @Override
  @NotAstProperty
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
  
  @Override
  public ParenthesesExpression clone() {
    ParenthesesExpression result = (ParenthesesExpression) super.clone();
    result.enclosedExpression = enclosedExpression==null?null:enclosedExpression.clone();
    result.configureParentToAllChilds();
    return result;
  }

}
