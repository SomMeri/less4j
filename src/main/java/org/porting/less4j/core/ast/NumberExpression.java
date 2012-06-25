package org.porting.less4j.core.ast;

import org.antlr.runtime.tree.CommonTree;

public class NumberExpression extends Expression {

  private Sign sign = Sign.NONE;
  private String valueAsString;
  @SuppressWarnings("unused")
  private Dimension dimension = Dimension.NUMBER;

  public NumberExpression(CommonTree token) {
    super(token);
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
    //TODO: this method will be needed when doing more complicated expressions
    throw new IllegalStateException("not implemented yet");
    //return dimension;
  }

  public void setDimension(Dimension dimension) {
    this.dimension = dimension;
    throw new IllegalStateException("not implemented yet");
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.NUMBER;
  }

  public enum Sign {
    PLUS, MINUS, NONE
  }

  public enum Dimension {
    NUMBER, PERCENTAGE, LENGTH, EMS, EXS, ANGLE, TIME, FREQ
  }
}
