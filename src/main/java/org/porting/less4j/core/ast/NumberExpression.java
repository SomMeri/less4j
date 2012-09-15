package org.porting.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

public class NumberExpression extends Expression implements Cloneable {

  private String originalString;
  private Dimension dimension = Dimension.NUMBER;
  private Double valueAsDouble;
  private String suffix = "";
  private boolean expliciteSign = false;

  public NumberExpression(HiddenTokenAwareTree token) {
    super(token);
  }

  public NumberExpression(HiddenTokenAwareTree token, String originalString) {
    super(token);
    this.originalString = originalString;
  }

  public NumberExpression(HiddenTokenAwareTree token, String originalString, Dimension dimension) {
    this(token, originalString);
    this.dimension = dimension;
  }

  public NumberExpression(HiddenTokenAwareTree token, Double valueAsDouble, String suffix, String originalString, Dimension dimension) {
    this(token, originalString, dimension);
    this.valueAsDouble = valueAsDouble;
    this.suffix = suffix;
  }

  public String getOriginalString() {
    return originalString;
  }

  public void setOriginalString(String originalString) {
    this.originalString = originalString;
  }

  public Dimension getDimension() {
    return dimension;
  }

  public Double getValueAsDouble() {
    return valueAsDouble;
  }

  public void setValueAsDouble(Double number) {
    this.valueAsDouble = number;
  }

  public void setDimension(Dimension dimension) {
    this.dimension = dimension;
  }

  public String getSuffix() {
    return suffix;
  }

  public boolean hasExpliciteSign() {
    return expliciteSign;
  }

  public void setExpliciteSign(boolean expliciteSign) {
    this.expliciteSign = expliciteSign;
  }

  public void setSuffix(String suffix) {
    this.suffix = suffix;
  }

  public void negate() {
    if (valueAsDouble == null)
      return;

    valueAsDouble = valueAsDouble * -1;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

  public boolean hasOriginalString() {
    return getOriginalString() != null;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.NUMBER;
  }

  public enum Dimension {
    NUMBER, PERCENTAGE, LENGTH, EMS, EXS, ANGLE, TIME, FREQ, REPEATER, UNKNOWN;
  }

  @Override
  public String toString() {
    if (originalString != null)
      return originalString;

    return "" + valueAsDouble + suffix;
  }

  @Override
  public NumberExpression clone() {
    NumberExpression clone = (NumberExpression) super.clone();
    return clone;
  }

}
