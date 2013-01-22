package com.github.sommeri.less4j.core.compiler.expressions;

import java.awt.Color;
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
import com.github.sommeri.less4j.utils.HSLColor;

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
  protected static final String LUME = "luma";

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
    FUNCTIONS.put(LIGHTEN, new Lighten());
    FUNCTIONS.put(DARKEN, new Darken());
    FUNCTIONS.put(HSLA, new HSLA());
    FUNCTIONS.put(HSL, new HSL());
    FUNCTIONS.put(RGBA, new RGBA());
    FUNCTIONS.put(RGB, new RGB());
    FUNCTIONS.put(ARGB, new ARGB());
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

class Lighten extends AbstractColorAmountFunction {

  @Override
  protected Expression evaluate(ColorExpression colorExpression, NumberExpression amount, HiddenTokenAwareTree token) {
    Color color = colorExpression.toColor();
    float[] hsl = HSLColor.fromRGB(color);
    hsl[2] += amount.getValueAsDouble();
    hsl[2] = clamp(hsl[2]);
    return hsla(hsl, color.getAlpha() / 255.0f, token);
  }

}

class Darken extends AbstractColorAmountFunction {

  protected Expression evaluate(ColorExpression colorExpression, NumberExpression amount, HiddenTokenAwareTree token) {
    Color color = colorExpression.toColor();
    float[] hsl = HSLColor.fromRGB(color);
    hsl[2] -= amount.getValueAsDouble();
    hsl[2] = clamp(hsl[2]);
    return hsla(hsl, color.getAlpha() / 255.0f, token);
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
    Color color = HSLColor.toRGB(h.getValueAsDouble().floatValue(), s.getValueAsDouble().floatValue(), l.getValueAsDouble().floatValue(),
        1.0f);
    return new ColorExpression.ColorWithAlphaExpression(token, color);
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
    Color color = HSLColor.toRGB(h.getValueAsDouble().floatValue(), s.getValueAsDouble().floatValue(), l.getValueAsDouble().floatValue(), a
        .getValueAsDouble().floatValue());
    return new ColorExpression.ColorWithAlphaExpression(token, color);
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
    return Math.min(100, Math.max(0, val));
  }

  static ColorExpression hsl(float[] hsl, HiddenTokenAwareTree token) {
    return hsla(hsl, 1.0f, token);
  }

  static ColorExpression hsla(float[] hsl, float a, HiddenTokenAwareTree token) {
    Color color = HSLColor.toRGB(hsl, a);
    return new ColorExpression.ColorWithAlphaExpression(token, color);
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
