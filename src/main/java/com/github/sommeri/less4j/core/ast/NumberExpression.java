package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  
  public NumberExpression convertTo(String targetUnit) {
    Map<Dimension, String> conversions = new HashMap<Dimension, String>();
    for (Dimension dim : Dimension.values()) {
      if (dim.getConversions().containsKey(targetUnit)) {
	conversions.put(dim, targetUnit);
      }
    }
    
    return convertTo(conversions);
  }
  
  public NumberExpression convertTo(Map<Dimension, String> conversions) {
    double value = getValueAsDouble();
    String unit = getSuffix();
    Dimension dimension = getDimension();

    String targetUnit = conversions.get(dimension);
    if (targetUnit == null) {
      /* No conversion requested for this type of number */
      return this;
    }

    Map<String, Number> group = dimension.getConversions();

    Number multiplier = group.get(unit);
    Number divisor = group.get(targetUnit);

    if (multiplier != null && divisor != null) {
      return new NumberExpression(getUnderlyingStructure(), value * multiplier.doubleValue() / divisor.doubleValue(), targetUnit, null, getDimension());
    } else {
      throw new IllegalArgumentException("Conversion between " + unit + " and " + targetUnit + " is not supported.");
    }
  }

  public enum Dimension {
    NUMBER, PERCENTAGE, LENGTH, EMS, EXS, ANGLE, TIME, FREQ, REPEATER, UNKNOWN;

    private Map<String, Number> conversions;

    public static Dimension forSuffix(String suffix) {
      if (suffix.equals("%")) {
        return PERCENTAGE;
      } else if (suffix.equals("px") || suffix.equals("cm") || suffix.equals("mm") || suffix.equals("in") || suffix.equals("pt") || suffix.equals("pc")) {
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

    public Map<String, Number> getConversions() {
      if (conversions != null)
        return conversions;

      HashMap<String, Number> result = new HashMap<String, Number>();
      switch (this) {
      case LENGTH:
        result.put("m", 1);
        result.put("cm", 0.01);
        result.put("mm", 0.001);
        result.put("in", 0.0254);
        result.put("pt", 0.0254 / 72);
        result.put("pc", 0.0254 / 72 * 12);
        break;

      case TIME:
        result.put("s", 1);
        result.put("ms", 0.001);
        break;

      case ANGLE:
        result.put("rad", 1.0 / (2 * Math.PI));
        result.put("deg", 1.0 / 360);
        result.put("grad", 1.0 / 400);
        result.put("turn", 1);
        break;

      default:
        break;
      }

      this.conversions = result;
      return result;
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
