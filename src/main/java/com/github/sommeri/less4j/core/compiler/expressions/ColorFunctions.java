package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.AnonymousExpression;
import com.github.sommeri.less4j.core.ast.ColorExpression;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression.Dimension;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class ColorFunctions implements FunctionsPackage {

  protected static final String RGB = "rgb";
  protected static final String RGBA = "rgba";
  protected static final String ARGB = "argb";
  protected static final String HSL = "hsl";
  protected static final String HSLA = "hsla";
  protected static final String HSV = "hsv";
  protected static final String HSVA = "hsva";

  protected static final String HUE = "hue";
  protected static final String SATURATION = "saturation";
  protected static final String LIGHTNESS = "lightness";
  protected static final String RED = "red";
  protected static final String GREEN = "green";
  protected static final String BLUE = "blue";
  protected static final String ALPHA = "alpha";
  protected static final String LUMA = "luma";

  protected static final String SATURATE = "saturate";
  protected static final String DESATURATE = "desaturate";
  protected static final String LIGHTEN = "lighten";
  protected static final String DARKEN = "darken";
  protected static final String FADEIN = "fadein";
  protected static final String FADEOUT = "fadeout";
  protected static final String FADE = "fade";
  protected static final String SPIN = "spin";
  protected static final String MIX = "mix";
  protected static final String GREYSCALE = "greyscale";
  protected static final String CONTRAST = "contrast";

  protected static final String MULTIPLY = "multiply";
  protected static final String SCREEN = "screen";
  protected static final String OVERLAY = "overlay";
  protected static final String SOFTLIGHT = "softlight";
  protected static final String HARDLIGHT = "hardlight";
  protected static final String DIFFERENCE = "difference";
  protected static final String EXCLUSION = "exclusion";
  protected static final String AVERAGE = "average";
  protected static final String NEGATION = "negation";

  private static Map<String, Function> FUNCTIONS = new HashMap<String, Function>();
  static {
    FUNCTIONS.put(RGB, new RGB());
    FUNCTIONS.put(RGBA, new RGBA());
    FUNCTIONS.put(ARGB, new ARGB());
    FUNCTIONS.put(HSL, new HSL());
    FUNCTIONS.put(HSLA, new HSLA());
    // FUNCTIONS.put(HSV, new HSV());
    // FUNCTIONS.put(HSVA, new HSVA());

    FUNCTIONS.put(HUE, new Hue());
    FUNCTIONS.put(SATURATION, new Saturation());
    FUNCTIONS.put(LIGHTNESS, new Lightness());
    FUNCTIONS.put(RED, new Red());
    FUNCTIONS.put(GREEN, new Green());
    FUNCTIONS.put(BLUE, new Blue());
    FUNCTIONS.put(ALPHA, new Alpha());
    FUNCTIONS.put(LUMA, new Luma());

    FUNCTIONS.put(SATURATE, new Saturate());
    FUNCTIONS.put(DESATURATE, new Desaturate());
    FUNCTIONS.put(LIGHTEN, new Lighten());
    FUNCTIONS.put(DARKEN, new Darken());
    FUNCTIONS.put(FADEIN, new FadeIn());
    FUNCTIONS.put(FADEOUT, new FadeOut());
    FUNCTIONS.put(FADE, new Fade());
    FUNCTIONS.put(SPIN, new Spin());
    FUNCTIONS.put(MIX, new Mix());
    FUNCTIONS.put(GREYSCALE, new Greyscale());
    FUNCTIONS.put(CONTRAST, new Contrast());

//    FUNCTIONS.put(MULTIPLY, new Multiply());
//    FUNCTIONS.put(SCREEN, new Screen());
//    FUNCTIONS.put(OVERLAY, new Overlay());
//    FUNCTIONS.put(SOFTLIGHT, new Softlight());
//    FUNCTIONS.put(HARDLIGHT, new Hardlight());
//    FUNCTIONS.put(DIFFERENCE, new Difference());
//    FUNCTIONS.put(EXCLUSION, new Exclusion());
//    FUNCTIONS.put(AVERAGE, new Average());
//    FUNCTIONS.put(NEGATION, new Negation());
  }

  private final ProblemsHandler problemsHandler;

  public ColorFunctions(ProblemsHandler problemsHandler) {
    this.problemsHandler = problemsHandler;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.sommeri.less4j.core.compiler.expressions.FunctionsPackage#
   * canEvaluate(com.github.sommeri.less4j.core.ast.FunctionExpression,
   * com.github.sommeri.less4j.core.ast.Expression)
   */
  @Override
  public boolean canEvaluate(FunctionExpression input, Expression parameters) {
    return FUNCTIONS.containsKey(input.getName());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.github.sommeri.less4j.core.compiler.expressions.FunctionsPackage#evaluate
   * (com.github.sommeri.less4j.core.ast.FunctionExpression,
   * com.github.sommeri.less4j.core.ast.Expression)
   */
  @Override
  public Expression evaluate(FunctionExpression input, Expression parameters) {
    if (!canEvaluate(input, parameters))
      return input;

    Function function = FUNCTIONS.get(input.getName());
    return function.evaluate(parameters, problemsHandler);
  }

}

class RGB extends AbstractColorFunction {

  @Override
  protected Expression evaluate(List<Expression> parameters, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    return evaluate((NumberExpression) parameters.get(0), (NumberExpression) parameters.get(1), (NumberExpression) parameters.get(2), token);
  }

  private Expression evaluate(NumberExpression r, NumberExpression g, NumberExpression b, HiddenTokenAwareTree token) {
    return new ColorExpression(token, (int) Math.round(scaled(r, 255)), (int) Math.round(scaled(g, 255)), (int) Math.round(scaled(b, 255)));
  }

  @Override
  protected boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler) {
    return validateParameter(parameter, ASTCssNodeType.NUMBER, problemsHandler);
  }

  @Override
  protected int getMinParameters() {
    return 3;
  }

  @Override
  protected int getMaxParameters() {
    return 3;
  }

}

class RGBA extends AbstractColorFunction {

  @Override
  protected Expression evaluate(List<Expression> parameters, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    return evaluate((NumberExpression) parameters.get(0), (NumberExpression) parameters.get(1), (NumberExpression) parameters.get(2),
        (NumberExpression) parameters.get(3), token);
  }

  private Expression evaluate(NumberExpression r, NumberExpression g, NumberExpression b, NumberExpression a, HiddenTokenAwareTree token) {
    return new ColorExpression.ColorWithAlphaExpression(token, scaled(r, 255), scaled(g, 255),
        scaled(b, 255), number(a));
  }

  @Override
  protected boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler) {
    return validateParameter(parameter, ASTCssNodeType.NUMBER, problemsHandler);
  }

  @Override
  protected int getMinParameters() {
    return 4;
  }

  @Override
  protected int getMaxParameters() {
    return 4;
  }

}

class HSL extends AbstractColorFunction {

  @Override
  protected Expression evaluate(List<Expression> parameters, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    return evaluate((NumberExpression) parameters.get(0), (NumberExpression) parameters.get(1), (NumberExpression) parameters.get(2), token);
  }

  private Expression evaluate(NumberExpression h, NumberExpression s, NumberExpression l, HiddenTokenAwareTree token) {
    return hsla(new HSLAValue(number(h), number(s), number(l)), token);
  }

  @Override
  protected boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler) {
    return validateParameter(parameter, ASTCssNodeType.NUMBER, problemsHandler);
  }

  @Override
  protected int getMinParameters() {
    return 3;
  }

  @Override
  protected int getMaxParameters() {
    return 3;
  }

}

class HSLA extends AbstractColorFunction {

  @Override
  protected Expression evaluate(List<Expression> parameters, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    return evaluate((NumberExpression) parameters.get(0), (NumberExpression) parameters.get(1), (NumberExpression) parameters.get(2),
        (NumberExpression) parameters.get(3), token);
  }

  private Expression evaluate(NumberExpression h, NumberExpression s, NumberExpression l, NumberExpression a, HiddenTokenAwareTree token) {
    return hsla(new HSLAValue(number(h), number(s), number(l), number(a)), token);
  }

  @Override
  protected boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler) {
    return validateParameter(parameter, ASTCssNodeType.NUMBER, problemsHandler);
  }

  @Override
  protected int getMinParameters() {
    return 4;
  }

  @Override
  protected int getMaxParameters() {
    return 4;
  }

}

class ARGB extends AbstractColorFunction {

  @Override
  protected Expression evaluate(List<Expression> parameters, ProblemsHandler problemHandler, HiddenTokenAwareTree token) {
    return new AnonymousExpression(token, ((ColorExpression) parameters.get(0)).toARGB());
  }

  @Override
  protected int getMaxParameters() {
    return 1;
  }

  @Override
  protected int getMinParameters() {
    return 1;
  }

  @Override
  protected boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler) {
    return validateParameter(parameter, ASTCssNodeType.COLOR_EXPRESSION, problemsHandler);
  }

}

class Hue extends AbstractColorOperationFunction {

  @Override
  protected Expression evaluate(ColorExpression color, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    HSLAValue hsla = toHSLA(color);
    return new NumberExpression(token, Double.valueOf(hsla.h), "", null, Dimension.NUMBER);
  }

}

class Saturation extends AbstractColorOperationFunction {

  @Override
  protected Expression evaluate(ColorExpression color, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    HSLAValue hsla = toHSLA(color);
    return new NumberExpression(token, Double.valueOf(hsla.s * 100), "%", null, Dimension.PERCENTAGE);
  }

}

class Lightness extends AbstractColorOperationFunction {

  @Override
  protected Expression evaluate(ColorExpression color, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    HSLAValue hsla = toHSLA(color);
    return new NumberExpression(token, Double.valueOf(hsla.l * 100), "%", null, Dimension.PERCENTAGE);
  }

}

class Red extends AbstractColorOperationFunction {

  @Override
  protected Expression evaluate(ColorExpression color, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    return new NumberExpression(token, Double.valueOf(color.getRed()), "", null, Dimension.NUMBER);
  }

}

class Green extends AbstractColorOperationFunction {

  @Override
  protected Expression evaluate(ColorExpression color, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    return new NumberExpression(token, Double.valueOf(color.getGreen()), "", null, Dimension.NUMBER);
  }

}

class Blue extends AbstractColorOperationFunction {

  @Override
  protected Expression evaluate(ColorExpression color, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    return new NumberExpression(token, Double.valueOf(color.getBlue()), "", null, Dimension.NUMBER);
  }

}

class Alpha extends AbstractColorOperationFunction {

  @Override
  protected Expression evaluate(ColorExpression color, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    return new NumberExpression(token, Double.valueOf(color.getAlpha()), "", null, Dimension.NUMBER);
  }

}

class Luma extends AbstractColorOperationFunction {

  @Override
  protected Expression evaluate(ColorExpression color, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    double luma = (Math
        .round((0.2126 * (color.getRed() / 255.0) + 0.7152 * (color.getGreen() / 255.0) + 0.0722 * (color.getBlue() / 255.0))
            * color.getAlpha() * 100));

    return new NumberExpression(token, Double.valueOf(luma), "%", null, Dimension.PERCENTAGE);
  }

}

abstract class AbstractColorOperationFunction extends AbstractColorFunction {

  @Override
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    return evaluate((ColorExpression) splitParameters.get(0), problemsHandler, token);
  }

  protected abstract Expression evaluate(ColorExpression color, ProblemsHandler problemsHandler, HiddenTokenAwareTree token);

  @Override
  protected int getMinParameters() {
    return 1;
  }

  @Override
  protected int getMaxParameters() {
    return 1;
  }

  @Override
  protected boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler) {
    return validateParameter(parameter, ASTCssNodeType.COLOR_EXPRESSION, problemsHandler);
  }

}

class Saturate extends AbstractColorHSLAmountFunction {

  @Override
  protected void apply(NumberExpression amount, HSLAValue hsla) {
    hsla.s += amount.getValueAsDouble() / 100.0f;
    hsla.s = clamp(hsla.s);
  }

}

class Desaturate extends AbstractColorHSLAmountFunction {

  @Override
  protected void apply(NumberExpression amount, HSLAValue hsla) {
    hsla.s -= amount.getValueAsDouble() / 100.0f;
    hsla.s = clamp(hsla.s);
  }

}

class Lighten extends AbstractColorHSLAmountFunction {

  @Override
  protected void apply(NumberExpression amount, HSLAValue hsla) {
    hsla.l += amount.getValueAsDouble() / 100.0f;
    hsla.l = clamp(hsla.l);
  }

}

class Darken extends AbstractColorHSLAmountFunction {

  @Override
  protected void apply(NumberExpression amount, HSLAValue hsla) {
    hsla.l -= amount.getValueAsDouble() / 100.0f;
    hsla.l = clamp(hsla.l);
  }

}

class FadeIn extends AbstractColorHSLAmountFunction {

  @Override
  protected void apply(NumberExpression amount, HSLAValue hsla) {
    hsla.a += amount.getValueAsDouble() / 100.0f;
    hsla.a = clamp(hsla.a);
  }

}

class FadeOut extends AbstractColorHSLAmountFunction {

  @Override
  protected void apply(NumberExpression amount, HSLAValue hsla) {
    hsla.a -= amount.getValueAsDouble() / 100.0f;
    hsla.a = clamp(hsla.a);
  }

}

class Fade extends AbstractColorHSLAmountFunction {

  @Override
  protected void apply(NumberExpression amount, HSLAValue hsla) {
    hsla.a = (amount.getValueAsDouble() / 100.0f);
    hsla.a = clamp(hsla.a);
  }

}

class Spin extends AbstractColorHSLAmountFunction {

  @Override
  protected void apply(NumberExpression amount, HSLAValue hsla) {
    double hue = ((hsla.h + amount.getValueAsDouble()) % 360);
    hsla.h = hue < 0 ? 360 + hue : hue;
  }

}

//
// Copyright (c) 2006-2009 Hampton Catlin, Nathan Weizenbaum, and Chris Eppstein
// http://sass-lang.com
//
class Mix extends AbstractColorFunction {

  @Override
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    ColorExpression color1 = (ColorExpression) splitParameters.get(0);
    ColorExpression color2 = (ColorExpression) splitParameters.get(1);
    NumberExpression weight = splitParameters.size() > 2 ? (NumberExpression) splitParameters.get(2) : null;
    
    if (weight == null) {
      weight = new NumberExpression(token, Double.valueOf(50), "%", null, Dimension.PERCENTAGE);
    }
    
    double p = weight.getValueAsDouble() / 100.0;
    double w = p * 2 - 1;
    double a = color1.getAlpha() - color2.getAlpha();
  
    double w1 = (((w * a == -1) ? w : (w + a) / (1 + w * a)) + 1) / 2.0;
    double w2 = 1 - w1;
  
    return new ColorExpression.ColorWithAlphaExpression(token, color1.getRed() * w1 + color2.getRed() * w2, 
        color1.getGreen() * w1 + color2.getGreen() * w2, 
        color1.getBlue() * w1 + color2.getBlue() * w2,
        color1.getAlpha() * p + color2.getAlpha() * (1 - p));
  }

  @Override
  protected int getMinParameters() {
    return 2;
  }

  @Override
  protected int getMaxParameters() {
    return 3;
  }

  @Override
  protected boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler) {
    switch (position) {
    case 0:
    case 1:
      return validateParameter(parameter, ASTCssNodeType.COLOR_EXPRESSION, problemsHandler);
    case 2:
      return validateParameter(parameter, ASTCssNodeType.NUMBER, problemsHandler);
    }
    return false;
  }

}

class Greyscale extends AbstractColorOperationFunction {

  @Override
  protected Expression evaluate(ColorExpression color, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    HSLAValue hsla = toHSLA(color);
    hsla.s = 0;
    return hsla(hsla, token);
  }
  
}

class Contrast extends AbstractMultiParameterFunction {

  @Override
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    ColorExpression color = (ColorExpression) splitParameters.get(0);
    ColorExpression dark = (ColorExpression) (splitParameters.size() > 1 ? splitParameters.get(1) : new ColorExpression(token, 0, 0, 0));
    ColorExpression light = (ColorExpression) (splitParameters.size() > 2 ? splitParameters.get(2) : new ColorExpression(token, 255, 255, 255));
    NumberExpression threshold = (NumberExpression) (splitParameters.size() > 3 ? splitParameters.get(3) : new NumberExpression(token, 43.0, "%", null, Dimension.PERCENTAGE));
    double thresholdValue = AbstractColorFunction.number(threshold);
    
    if (((0.2126 * (color.getRed()/255) + 0.7152 * (color.getGreen()/255) + 0.0722 * (color.getBlue()/255)) * color.getAlpha()) < thresholdValue) {
      return light;
    } else {
      return dark;
    }
  }

  @Override
  protected int getMinParameters() {
    return 1;
  }

  @Override
  protected int getMaxParameters() {
    return 4;
  }

  @Override
  protected boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler) {
    switch (position) {
    case 0:
      return (parameter.getType() == ASTCssNodeType.COLOR_EXPRESSION);
    case 1:
    case 2:
      return validateParameter(parameter, ASTCssNodeType.COLOR_EXPRESSION, problemsHandler);
    case 3:
      return validateParameter(parameter, ASTCssNodeType.NUMBER, problemsHandler);
    }
    return false;
  }

  
}

abstract class AbstractColorFunction extends AbstractMultiParameterFunction {

  static double clamp(double val) {
    return Math.min(1, Math.max(0, val));
  }

  static ColorExpression hsla(HSLAValue hsla, HiddenTokenAwareTree token) {
    double h = (hsla.h % 360) / 360, s = hsla.s, l = hsla.l, a = hsla.a;

    double m2 = l <= 0.5 ? l * (s + 1) : l + s - l * s;
    double m1 = l * 2 - m2;

    return new ColorExpression.ColorWithAlphaExpression(token, hue(h + 1.0 / 3.0, m1, m2) * 255, hue(h,
        m1, m2) * 255, hue(h - 1.0 / 3.0, m1, m2) * 255, a);
  }

  static double hue(double h, double m1, double m2) {
    h = h < 0 ? h + 1 : (h > 1 ? h - 1 : h);
    if (h * 6 < 1)
      return m1 + (m2 - m1) * h * 6;
    else if (h * 2 < 1)
      return m2;
    else if (h * 3 < 2)
      return m1 + (m2 - m1) * (2.0 / 3.0 - h) * 6;
    else
      return m1;
  }

  static double scaled(NumberExpression n, int size) {
    if (n.getDimension() == Dimension.PERCENTAGE) {
      return n.getValueAsDouble() * size / 100;
    } else {
      return number(n);
    }
  }

  static double number(NumberExpression n) {
    if (n.getDimension() == Dimension.PERCENTAGE) {
      return n.getValueAsDouble() / 100;
    } else {
      return n.getValueAsDouble();
    }
  }

  static HSLAValue toHSLA(ColorExpression color) {
    double r = color.getRed() / 255.0, g = color.getGreen() / 255.0, b = color.getBlue() / 255.0, a = color.getAlpha();

    double max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b));
    double h, s, l = (max + min) / 2, d = max - min;

    if (max == min) {
      h = s = 0;
    } else {
      s = l > 0.5 ? d / (2 - max - min) : d / (max + min);

      if (max == r) {
        h = (g - b) / d + (g < b ? 6 : 0);
      } else if (max == g) {
        h = (b - r) / d + 2;
      } else {
        h = (r - g) / d + 4;
      }

      h /= 6;
    }

    return new HSLAValue((h * 360), s, l, a);
  }

}

class HSLAValue {
  public double h, s, l, a;

  public HSLAValue() {
    super();
  }

  public HSLAValue(double h, double s, double l, double a) {
    super();
    this.h = h;
    this.s = s;
    this.l = l;
    this.a = a;
  }

  public HSLAValue(double h, double s, double l) {
    super();
    this.h = h;
    this.s = s;
    this.l = l;
    this.a = 1.0f;
  }
}

abstract class AbstractColorAmountFunction extends AbstractColorFunction {

  @Override
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    ColorExpression color = (ColorExpression) splitParameters.get(0);
    NumberExpression amount = (NumberExpression) splitParameters.get(1);

    return evaluate(color, amount, token);
  }

  protected abstract Expression evaluate(ColorExpression color, NumberExpression amount, HiddenTokenAwareTree token);

  @Override
  protected int getMinParameters() {
    return 2;
  }

  @Override
  protected int getMaxParameters() {
    return 2;
  }

  @Override
  protected boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler) {
    switch (position) {
    case 0:
      return validateParameter(parameter, ASTCssNodeType.COLOR_EXPRESSION, problemsHandler);
    case 1:
      return validateParameter(parameter, ASTCssNodeType.NUMBER, problemsHandler);
    }
    return false;
  }

}

abstract class AbstractColorHSLAmountFunction extends AbstractColorAmountFunction {

  @Override
  protected Expression evaluate(ColorExpression color, NumberExpression amount, HiddenTokenAwareTree token) {
    HSLAValue hsla = toHSLA(color);

    apply(amount, hsla);

    return hsla(hsla, token);
  }

  /**
   * Apply the amount to the given hsla array.
   * 
   * @param amount
   * @param hsla
   */
  protected abstract void apply(NumberExpression amount, HSLAValue hsla);

}
