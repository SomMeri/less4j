package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class InterpolatedMediaExpression extends MediaExpression {

  private Expression expression;
  
  public InterpolatedMediaExpression(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  public InterpolatedMediaExpression(HiddenTokenAwareTree underlyingStructure, Expression expression) {
    this(underlyingStructure);
    this.expression = expression;
  }

  public Expression getExpression() {
    return expression;
  }

  public void setExpression(Expression expression) {
    this.expression = expression;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(expression);
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.INTERPOLATED_MEDIA_EXPRESSION;
  }

  public InterpolatedMediaExpression clone() {
    return (InterpolatedMediaExpression) super.clone();
  }

}
