package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

//the system would be nicer and more consistent if they all would be cloneable. 
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
    
    public static Dimension forSuffix(String suffix) {
      if (suffix.equals("%")) {
	return PERCENTAGE;
      } else if (suffix.equals("px") || suffix.equals("cm") || suffix.equals("mm") || suffix.equals("in") ||
	  suffix.equals("pt") || suffix.equals("pc")) {
	return LENGTH;
      } else if (suffix.equals("em")) {
	return EMS;
      } else if (suffix.equals("ex")) {
	return EXS;
      } else if (suffix.equals("deg") || suffix.equals("rad") || suffix.equals("grad")) {
	return ANGLE;
      } else if (suffix.equals("ms") || suffix.equals("s")) {
	return TIME;
      } else if (suffix.equals("khz") || suffix.equals("hz")) {
	return FREQ;
      } else {
	return UNKNOWN;
      }
    }
  }

  @Override
  public String toString() {
    if (originalString != null)
      return originalString;

    return "" + valueAsDouble + suffix;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((dimension == null) ? 0 : dimension.hashCode());
    result = prime * result + (expliciteSign ? 1231 : 1237);
    result = prime * result + ((suffix == null) ? 0 : suffix.hashCode());
    result = prime * result + ((valueAsDouble == null) ? 0 : valueAsDouble.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    NumberExpression other = (NumberExpression) obj;
    if (dimension != other.dimension)
      return false;
    if (expliciteSign != other.expliciteSign)
      return false;
    if (suffix == null) {
      if (other.suffix != null)
        return false;
    } else if (!suffix.equals(other.suffix))
      return false;
    if (valueAsDouble == null) {
      if (other.valueAsDouble != null)
        return false;
    } else if (!valueAsDouble.equals(other.valueAsDouble))
      return false;
    return true;
  }

  @Override
  public NumberExpression clone() {
    NumberExpression clone = (NumberExpression) super.clone();
    return clone;
  }

}
