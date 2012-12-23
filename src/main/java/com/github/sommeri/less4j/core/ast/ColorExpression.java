package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class ColorExpression extends Expression {

  private String value;
  private int red;
  private int green;
  private int blue;

  public ColorExpression(HiddenTokenAwareTree token, String value) {
    super(token);
    setValue(value);
  }

  public ColorExpression(HiddenTokenAwareTree token, int red, int green, int blue) {
    super(token);
    this.red = red;
    this.green = green;
    this.blue = blue;
    this.value = encode(red, green, blue);
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
    red=decode(value, 0);
    green=decode(value, 1);
    blue=decode(value, 2);
  }

  public int getRed() {
    return red;
  }

  public int getGreen() {
    return green;
  }

  public int getBlue() {
    return blue;
  }
  
  private int decode(String color, int i) {
    if (color.length()<7) {
      String substring = color.substring(i+1, i+2);
      return Integer.parseInt(substring+substring, 16);
    }
    
    return Integer.parseInt(color.substring(i*2+1, i*2+3), 16);
  }

  private String encode(int red, int green, int blue) {
    return "#" + toHex(red) + toHex(green) + toHex(blue); 
  }

  private String toHex(int color) {
    String prefix = "";
    if (color<16)
      prefix = "0";
    return prefix + Integer.toHexString(color);
  }

  @Override
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
}
