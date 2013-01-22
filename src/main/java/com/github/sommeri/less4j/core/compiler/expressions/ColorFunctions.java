package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.AnonymousExpression;
import com.github.sommeri.less4j.core.ast.ColorExpression;
import com.github.sommeri.less4j.core.ast.ComposedExpression;
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
    
    FUNCTIONS.put(HUE, new Hue());
//    FUNCTIONS.put(SATURATION, new Saturation());
//    FUNCTIONS.put(LIGHTNESS, new Lightness());
//    FUNCTIONS.put(RED, new Red());
//    FUNCTIONS.put(GREEN, new Green());
//    FUNCTIONS.put(BLUE, new Blue());
//    FUNCTIONS.put(ALPHA, new Alpha());
//    FUNCTIONS.put(LUMA, new Luma());

    FUNCTIONS.put(SATURATE, new Saturate());
    FUNCTIONS.put(DESATURATE, new Desaturate());
    FUNCTIONS.put(LIGHTEN, new Lighten());
    FUNCTIONS.put(DARKEN, new Darken());
    FUNCTIONS.put(FADEIN, new FadeIn());
    FUNCTIONS.put(FADEOUT, new FadeOut());
    FUNCTIONS.put(FADE, new Fade());
    FUNCTIONS.put(SPIN, new Spin());
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
    return new ColorExpression.ColorWithAlphaExpression(token, (int) Math.round(scaled(r, 255)), (int) Math.round(scaled(g, 255)),
        (int) Math.round(scaled(b, 255)), (float) number(a));
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
    return hsla(new HSLAValue((float)number(h), (float)number(s), (float)number(l)), token);
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
    return hsla(new HSLAValue((float)number(h), (float)number(s), (float)number(l), (float)number(a)), token);
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
    return new AnonymousExpression(token, ((ColorExpression)parameters.get(0)).toARGB());
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
    return new NumberExpression(token, Double.valueOf(hsla.h), "", "" + hsla.h, Dimension.NUMBER);
  }

}

abstract class AbstractColorOperationFunction extends AbstractColorFunction {

  @Override
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    return evaluate((ColorExpression)splitParameters.get(0), problemsHandler, token);
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
    hsla.a = (float) (amount.getValueAsDouble() / 100.0f);
    hsla.a = clamp(hsla.a);
  }

}

class Spin extends AbstractColorHSLAmountFunction {

  @Override
  protected void apply(NumberExpression amount, HSLAValue hsla) {
    float hue = (float) ((hsla.h + amount.getValueAsDouble()) % 360);
    hsla.h = hue < 0 ? 360 + hue : hue;
  }

}

abstract class AbstractMultiParameterFunction implements Function {

  @Override
  public Expression evaluate(Expression parameters, ProblemsHandler problemsHandler) {
    List<Expression> splitParameters;

    if (getMinParameters() == 1 && parameters.getType() != ASTCssNodeType.COMPOSED_EXPRESSION) {
      splitParameters = Collections.singletonList(parameters);
    } else if (parameters.getType() == ASTCssNodeType.COMPOSED_EXPRESSION) {
      splitParameters = ((ComposedExpression) parameters).splitByComma();
    } else {
      problemsHandler.wrongNumberOfArgumentsToFunction(parameters, getMinParameters());
      return parameters;
    }

    if (splitParameters.size() >= getMinParameters() && splitParameters.size() <= getMaxParameters()) {
      /* Validate */
      boolean valid = true;
      for (int i = 0; i < splitParameters.size(); i++) {
        if (!validateParameter(splitParameters.get(i), i, problemsHandler)) {
          valid = false;
        }
      }

      if (valid) {
        return evaluate(splitParameters, problemsHandler, parameters.getUnderlyingStructure());
      } else {
        return parameters;
      }
    } else {
      problemsHandler.wrongNumberOfArgumentsToFunction(parameters, getMinParameters());
      return parameters;
    }
  }

  protected abstract Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, HiddenTokenAwareTree token);

  protected abstract int getMinParameters();

  protected abstract int getMaxParameters();

  protected abstract boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler);

  protected boolean validateParameter(Expression parameter, ASTCssNodeType expected, ProblemsHandler problemsHandler) {
    if (parameter.getType() != expected) {
      problemsHandler.wrongArgumentTypeToFunction(parameter, expected);
      return false;
    } else {
      return true;
    }
  }

}

abstract class AbstractColorFunction extends AbstractMultiParameterFunction {

  static float clamp(float val) {
    return Math.min(1, Math.max(0, val));
  }

  static ColorExpression hsla(HSLAValue hsla, HiddenTokenAwareTree token) {
    double h = (hsla.h % 360) / 360, s = hsla.s, l = hsla.l, a = hsla.a;

    double m2 = l <= 0.5 ? l * (s + 1) : l + s - l * s;
    double m1 = l * 2 - m2;

    return new ColorExpression.ColorWithAlphaExpression(token, (int)Math.round(hue(h + 1.0/3.0, m1, m2) * 255),
                     (int)Math.round(hue(h, m1, m2)       * 255),
                     (int)Math.round(hue(h - 1.0/3.0, m1, m2) * 255),
                     (float) a);
  }
  
  static double hue(double h, double m1, double m2) {
    h = h < 0 ? h + 1 : (h > 1 ? h - 1 : h);
    if      (h * 6 < 1) return m1 + (m2 - m1) * h * 6;
    else if (h * 2 < 1) return m2;
    else if (h * 3 < 2) return m1 + (m2 - m1) * (2.0/3.0 - h) * 6;
    else                return m1;
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
    double r = color.getRed() / 255.0f,
        g = color.getGreen() / 255.0f,
        b = color.getBlue() / 255.0f,
        a = color.getAlpha();
    
    double max = Math.max(r, Math.max(g, b)),
        min = Math.min(r, Math.min(g, b));
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
    
    return new HSLAValue((float) (h * 360), (float)s, (float)l, (float)a);
  }

}

class HSLAValue {
  public float h, s, l, a;

  public HSLAValue() {
    super();
  }

  public HSLAValue(float h, float s, float l, float a) {
    super();
    this.h = h;
    this.s = s;
    this.l = l;
    this.a = a;
  }

  public HSLAValue(float h, float s, float l) {
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
   * @param amount
   * @param hsla
   */
  protected abstract void apply(NumberExpression amount, HSLAValue hsla);
  
}
