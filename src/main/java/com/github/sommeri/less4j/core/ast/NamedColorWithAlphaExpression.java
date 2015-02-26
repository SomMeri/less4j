package com.github.sommeri.less4j.core.ast;

import com.github.sommeri.less4j.core.ast.ColorExpression.ColorWithAlphaExpression;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

/**
 * The list of all valid colors and construction methods are in {@link NamedColorWithAlphaExpression}
 *
 */
public class NamedColorWithAlphaExpression extends ColorWithAlphaExpression {
  
  private String colorName;

  public NamedColorWithAlphaExpression(HiddenTokenAwareTree token, String colorName, ColorExpression color) {
    super(token, color);
    this.colorName = colorName;
  }

  public String getValue() {
    return getColorName();
  }

  public String getColorName() {
    return colorName;
  }

  public void setColorName(String colorName) {
    this.colorName = colorName;
  }
  
  public boolean isNamed() {
    return true;
  }
  
  @Override
  public NamedColorWithAlphaExpression clone() {
    return (NamedColorWithAlphaExpression) super.clone();
  }
}
