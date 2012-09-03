package org.porting.less4j.core.ast;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

public class MediaExpression extends ASTCssNode {

  private String feature;
  private Expression expression;

  public MediaExpression(HiddenTokenAwareTree underlyingStructure, String feature, Expression expression) {
    super(underlyingStructure);
    this.feature = feature;
    this.expression = expression;
  }

  public String getFeature() {
    return feature;
  }

  public void setFeature(String feature) {
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
}
