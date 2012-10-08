package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class MediaExpression extends ASTCssNode {

  private MediaExpressionFeature feature;
  private Expression expression;

  public MediaExpression(HiddenTokenAwareTree underlyingStructure, MediaExpressionFeature feature, Expression expression) {
    super(underlyingStructure);
    this.feature = feature;
    this.expression = expression;
  }

  public MediaExpressionFeature getFeature() {
    return feature;
  }

  public void setFeature(MediaExpressionFeature feature) {
    this.feature = feature;
  }

  public Expression getExpression() {
    return expression;
  }

  public void setExpression(Expression expression) {
    this.expression = expression;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.MEDIA_EXPRESSION;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(feature, expression);
  }
  
  @Override
  public MediaExpression clone() {
    MediaExpression result = (MediaExpression) super.clone();
    result.feature = feature==null?null:feature.clone();
    result.expression = expression==null?null:expression.clone();
    result.configureParentToAllChilds();
    return result;
  }

}
