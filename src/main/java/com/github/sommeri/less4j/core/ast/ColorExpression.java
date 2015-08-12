package com.github.sommeri.less4j.core.ast;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.PrintUtils;

public class ColorExpression extends Expression {

  protected String value;
  protected double red;
  protected double green;
  protected double blue;

  ColorExpression(HiddenTokenAwareTree token, ColorExpression colorExpression) {
    super(token);
    red = colorExpression.red;
    green = colorExpression.green;
    blue = colorExpression.blue;
    this.value = encode(red, green, blue);
  }

  public ColorExpression(HiddenTokenAwareTree token, double red, double green, double blue) {
    super(token);
    this.red = red;
    this.green = green;
    this.blue = blue;
    this.value = encode(red, green, blue);
  }

  public ColorExpression(HiddenTokenAwareTree token, String value, double red, double green, double blue) {
    super(token);
    this.red = red;
    this.green = green;
    this.blue = blue;
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public boolean isShorthand() {
    String value = getValue();
    return !isNamed() && value!=null && value.length() < 7;
  }
  
  public String getValueInHexadecimal() {
    return encode(red, green, blue);
  }

  public void setValue(String value) {
    this.value = value;
    red = decode(value, 0);
    green = decode(value, 1);
    blue = decode(value, 2);
  }

  public double getRed() {
    return red;
  }

  public double getGreen() {
    return green;
  }

  public double getBlue() {
    return blue;
  }

  public double getAlpha() {
    return 1.0f;
  }

  private int decode(String color, int i) {
    if (color.length() < 7) {
      String substring = color.substring(i + 1, i + 2);
      return Integer.parseInt(substring + substring, 16);
    }

    return Integer.parseInt(color.substring(i * 2 + 1, i * 2 + 3), 16);
  }

  private String encode(double red, double green, double blue) {
    return "#" + toHex(red) + toHex(green) + toHex(blue);
  }

  protected String toHex(double color) {
    String prefix = "";
    if (color < 16)
      prefix = "0";
    return prefix + Integer.toHexString((int) Math.round(color));
  }

  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.COLOR_EXPRESSION;
  }

  @Override
  public String toString() {
    return "" + value;
  }

  @Override
  public ColorExpression clone() {
    return (ColorExpression) super.clone();
  }

  public String toARGB() {
    return "#ff" + toHex(red) + toHex(green) + toHex(blue);
  }

  public Color toColor() {
    return new Color((int) Math.round(this.red), (int) Math.round(this.green), (int) Math.round(this.blue));
  }

  public boolean hasAlpha() {
    return false;
  }

  public boolean isNamed() {
    return false;
  }

  public String getColorName() {
    return null;
  }

  public static class ColorWithAlphaExpression extends ColorExpression {

    /**
     * Alpha in the range 0-1.
     */
    private double alpha;

    public ColorWithAlphaExpression(HiddenTokenAwareTree token, ColorExpression colorExpression) {
      super(token, colorExpression);
      alpha = colorExpression.getAlpha();
      this.value = encode(red, green, blue, alpha);
    }

    public ColorWithAlphaExpression(HiddenTokenAwareTree token, double red, double green, double blue, double alpha) {
      super(token, red, green, blue);
      this.alpha = alpha;
      if (alpha != 1.0) {
        this.value = encode(red, green, blue, alpha);
      }
    }

    public ColorWithAlphaExpression(HiddenTokenAwareTree token, String originalValue, double red, double green, double blue, double alpha) {
      super(token, originalValue, red, green, blue);
      this.alpha = alpha;
    }

    @Override
    public double getAlpha() {
      return alpha;
    }

    protected String encode(double red, double green, double blue, double alpha) {
      return "rgba(" + roundFormat(red) + ", " + roundFormat(green) + ", " + roundFormat(blue) + ", " + roundFormatAlpha(alpha) + ")";
    }

    //alpha colors do not have hexadecimal value for now
    @Override
    public String getValueInHexadecimal() {
      return getValue();
    }
    
    private String roundFormat(double value) {
      return PrintUtils.formatNumber(Math.round(value));
    }

    private String roundFormatAlpha(double value) {
      return PrintUtils.formatNumberTwoDecimal(Math.round(value*1000000)/1000000.0);
    }

    @Override
    public String toARGB() {
      return "#" + toHex(Math.round(alpha * 255)) + toHex(red) + toHex(green) + toHex(blue);
    }

    @Override
    public Color toColor() {
      return new Color((int) Math.round(this.red), (int) Math.round(this.green), (int) Math.round(this.blue), Math.round(this.alpha * 255));
    }

    public boolean hasAlpha() {
      return true;
    }
  }

}
