package org.porting.less4j.core.ast;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

public class NumberExpression extends Expression {

  private Sign sign = Sign.NONE;
  private String valueAsString;
  private Dimension dimension = Dimension.NUMBER;

  public NumberExpression(HiddenTokenAwareTree token) {
    super(token);
  }

  public NumberExpression(HiddenTokenAwareTree token, String valueAsString) {
    super(token);
    this.valueAsString = valueAsString;
  }

  public NumberExpression(HiddenTokenAwareTree token, String valueAsString, Sign sign, Dimension dimension) {
    this(token, valueAsString);
    this.sign = sign;
    this.dimension = dimension;
  }

  public Sign getSign() {
    return sign;
  }

  public void setSign(Sign sign) {
    this.sign = sign;
  }

  public String getValueAsString() {
    return valueAsString;
  }

  public void setValueAsString(String valueAsString) {
    this.valueAsString = valueAsString;
  }

  public Dimension getDimension() {
    return dimension;
  }

  public void setDimension(Dimension dimension) {
    this.dimension = dimension;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.NUMBER;
  }

  public enum Sign {
    PLUS, MINUS, NONE
  }

  public enum Dimension {
    NUMBER, PERCENTAGE, LENGTH, EMS, EXS, ANGLE, TIME, FREQ, REPEATER, NONE;
  }
}
