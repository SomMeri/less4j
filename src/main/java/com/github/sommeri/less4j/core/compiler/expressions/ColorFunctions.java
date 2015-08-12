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

public class ColorFunctions extends BuiltInFunctionsPack {

  protected static final String RGB = "rgb";
  protected static final String RGBA = "rgba";
  protected static final String ARGB = "argb";
  protected static final String HSL = "hsl";
  protected static final String HSLA = "hsla";
  protected static final String HSV = "hsv";
  protected static final String HSVA = "hsva";
  protected static final String HSV_HUE = "hsvhue";
  protected static final String HSV_SATURATION = "hsvsaturation";
  protected static final String HSV_VALUE = "hsvvalue";

  protected static final String HUE = "hue";
  protected static final String SATURATION = "saturation";
  protected static final String LIGHTNESS = "lightness";
  protected static final String RED = "red";
  protected static final String GREEN = "green";
  protected static final String BLUE = "blue";
  protected static final String ALPHA = "alpha";
  protected static final String LUMA = "luma";
  protected static final String LUMINANCE = "luminance";

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

  protected static final String TINT = "tint";
  protected static final String SHADE = "shade"; 

  private static Map<String, Function> FUNCTIONS = new HashMap<String, Function>();
  static {
    FUNCTIONS.put(RGB, new RGB());
    FUNCTIONS.put(RGBA, new RGBA());
    FUNCTIONS.put(ARGB, new ARGB());
    FUNCTIONS.put(HSL, new HSL());
    FUNCTIONS.put(HSLA, new HSLA());
    FUNCTIONS.put(HSV, new HSV());
    FUNCTIONS.put(HSVA, new HSVA());
    FUNCTIONS.put(HSV_HUE, new HSVHue());
    FUNCTIONS.put(HSV_SATURATION, new HSVSaturation());
    FUNCTIONS.put(HSV_VALUE, new HSVValue());
    
    

    FUNCTIONS.put(HUE, new Hue());
    FUNCTIONS.put(SATURATION, new Saturation());
    FUNCTIONS.put(LIGHTNESS, new Lightness());
    FUNCTIONS.put(RED, new Red());
    FUNCTIONS.put(GREEN, new Green());
    FUNCTIONS.put(BLUE, new Blue());
    FUNCTIONS.put(ALPHA, new Alpha());
    FUNCTIONS.put(LUMA, new Luma());
    FUNCTIONS.put(LUMINANCE, new Luminance());

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

    FUNCTIONS.put(MULTIPLY, new Multiply());
    FUNCTIONS.put(SCREEN, new Screen());
    FUNCTIONS.put(OVERLAY, new Overlay());
    FUNCTIONS.put(SOFTLIGHT, new Softlight());
    FUNCTIONS.put(HARDLIGHT, new Hardlight());
    FUNCTIONS.put(DIFFERENCE, new Difference());
    FUNCTIONS.put(EXCLUSION, new Exclusion());
    FUNCTIONS.put(AVERAGE, new Average());
    FUNCTIONS.put(NEGATION, new Negation());

    FUNCTIONS.put(TINT, new Tint());
    FUNCTIONS.put(SHADE, new Shade());
  }

  public ColorFunctions(ProblemsHandler problemsHandler) {
    super(problemsHandler);
  }

  @Override
  protected Map<String, Function> getFunctions() {
    return FUNCTIONS;
  }

}

class RGB extends AbstractColorFunction {

  @Override
  protected Expression evaluate(List<Expression> parameters, ProblemsHandler problemsHandler, FunctionExpression functionCall, HiddenTokenAwareTree token) {
    return evaluate((NumberExpression) parameters.get(0), (NumberExpression) parameters.get(1), (NumberExpression) parameters.get(2), token);
  }

  private Expression evaluate(NumberExpression r, NumberExpression g, NumberExpression b, HiddenTokenAwareTree token) {
    return rgb(scaled(r, 255), scaled(g, 255), scaled(b, 255), token);
  }

  @Override
  protected boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler) {
    return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.NUMBER);
  }

  @Override
  protected int getMinParameters() {
    return 3;
  }

  @Override
  protected int getMaxParameters() {
    return 3;
  }

  @Override
  protected String getName() {
    return ColorFunctions.RGB;
  }

}

class RGBA extends AbstractColorFunction {

  @Override
  protected Expression evaluate(List<Expression> parameters, ProblemsHandler problemsHandler, FunctionExpression functionCall, HiddenTokenAwareTree token) {
    return evaluate((NumberExpression) parameters.get(0), (NumberExpression) parameters.get(1), (NumberExpression) parameters.get(2), (NumberExpression) parameters.get(3), token);
  }

  private Expression evaluate(NumberExpression r, NumberExpression g, NumberExpression b, NumberExpression a, HiddenTokenAwareTree token) {
    return rgba(scaled(r, 255), scaled(g, 255), scaled(b, 255), number(a), token);
  }

  @Override
  protected boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler) {
    return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.NUMBER);
  }

  @Override
  protected int getMinParameters() {
    return 4;
  }

  @Override
  protected int getMaxParameters() {
    return 4;
  }

  @Override
  protected String getName() {
    return ColorFunctions.RGBA;
  }

}

class HSL extends AbstractColorFunction {

  @Override
  protected Expression evaluate(List<Expression> parameters, ProblemsHandler problemsHandler, FunctionExpression functionCall, HiddenTokenAwareTree token) {
    return evaluate((NumberExpression) parameters.get(0), (NumberExpression) parameters.get(1), (NumberExpression) parameters.get(2), token);
  }

  private Expression evaluate(NumberExpression h, NumberExpression s, NumberExpression l, HiddenTokenAwareTree token) {
    return hsla(new HSLAValue(number(h), number(s), number(l)), token);
  }

  @Override
  protected boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler) {
    return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.NUMBER);
  }

  @Override
  protected int getMinParameters() {
    return 3;
  }

  @Override
  protected int getMaxParameters() {
    return 3;
  }

  @Override
  protected String getName() {
    return ColorFunctions.HSL;
  }

}

class HSLA extends AbstractColorFunction {

  @Override
  protected Expression evaluate(List<Expression> parameters, ProblemsHandler problemsHandler, FunctionExpression functionCall, HiddenTokenAwareTree token) {
    return evaluate((NumberExpression) parameters.get(0), (NumberExpression) parameters.get(1), (NumberExpression) parameters.get(2), (NumberExpression) parameters.get(3), token);
  }

  private Expression evaluate(NumberExpression h, NumberExpression s, NumberExpression l, NumberExpression a, HiddenTokenAwareTree token) {
    return hsla(new HSLAValue(number(h), number(s), number(l), number(a)), token);
  }

  @Override
  protected boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler) {
    return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.NUMBER);
  }

  @Override
  protected int getMinParameters() {
    return 4;
  }

  @Override
  protected int getMaxParameters() {
    return 4;
  }

  @Override
  protected String getName() {
    return ColorFunctions.HSLA;
  }

}

class HSV extends AbstractColorFunction {

  @Override
  protected Expression evaluate(List<Expression> parameters, ProblemsHandler problemsHandler, FunctionExpression functionCall, HiddenTokenAwareTree token) {
    return evaluate((NumberExpression) parameters.get(0), (NumberExpression) parameters.get(1), (NumberExpression) parameters.get(2), token);
  }

  private Expression evaluate(NumberExpression h, NumberExpression s, NumberExpression v, HiddenTokenAwareTree token) {
    return hsva(number(h), number(s), number(v), 1.0, token);
  }

  @Override
  protected boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler) {
    return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.NUMBER);
  }

  @Override
  protected int getMinParameters() {
    return 3;
  }

  @Override
  protected int getMaxParameters() {
    return 3;
  }

  @Override
  protected String getName() {
    return ColorFunctions.HSV;
  }

}

class HSVA extends AbstractColorFunction {

  @Override
  protected Expression evaluate(List<Expression> parameters, ProblemsHandler problemsHandler, FunctionExpression functionCall, HiddenTokenAwareTree token) {
    return evaluate((NumberExpression) parameters.get(0), (NumberExpression) parameters.get(1), (NumberExpression) parameters.get(2), (NumberExpression) parameters.get(3), token);
  }

  private Expression evaluate(NumberExpression h, NumberExpression s, NumberExpression v, NumberExpression a, HiddenTokenAwareTree token) {
    return hsva(number(h), number(s), number(v), number(a), token);
  }

  @Override
  protected boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler) {
    return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.NUMBER);
  }

  @Override
  protected int getMinParameters() {
    return 4;
  }

  @Override
  protected int getMaxParameters() {
    return 4;
  }

  @Override
  protected String getName() {
    return ColorFunctions.HSVA;
  }

}

class ARGB extends AbstractColorFunction {

  @Override
  protected Expression evaluate(List<Expression> parameters, ProblemsHandler problemHandler, FunctionExpression functionCall, HiddenTokenAwareTree token) {
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
    return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.COLOR_EXPRESSION);
  }

  @Override
  protected String getName() {
    return ColorFunctions.ARGB;
  }

}

class Hue extends AbstractColorOperationFunction {

  @Override
  protected Expression evaluate(ColorExpression color, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    HSLAValue hsla = toHSLA(color);
    return new NumberExpression(token, Double.valueOf(Math.round(hsla.h)), "", null, Dimension.NUMBER);
  }

  @Override
  protected String getName() {
    return ColorFunctions.HUE;
  }

}

class Saturation extends AbstractColorOperationFunction {

  @Override
  protected Expression evaluate(ColorExpression color, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    HSLAValue hsla = toHSLA(color);
    return new NumberExpression(token, Double.valueOf(Math.round(hsla.s * 100)), "%", null, Dimension.PERCENTAGE);
  }

  @Override
  protected String getName() {
    return ColorFunctions.SATURATION;
  }

}

class Lightness extends AbstractColorOperationFunction {

  @Override
  protected Expression evaluate(ColorExpression color, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    HSLAValue hsla = toHSLA(color);
    return new NumberExpression(token, Double.valueOf(Math.round(hsla.l * 100)), "%", null, Dimension.PERCENTAGE);
  }

  @Override
  protected String getName() {
    return ColorFunctions.LIGHTNESS;
  }

}

class HSVHue extends AbstractColorOperationFunction {

  @Override
  protected Expression evaluate(ColorExpression color, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    HSVAValue hsva = toHSVA(color);
    return new NumberExpression(token, Double.valueOf(Math.round(hsva.h)), "", null, Dimension.NUMBER);
  }

  @Override
  protected String getName() {
    return ColorFunctions.HSV_HUE;
  }

}

class HSVSaturation extends AbstractColorOperationFunction {

  @Override
  protected Expression evaluate(ColorExpression color, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    HSVAValue hsva = toHSVA(color);
    return new NumberExpression(token, Double.valueOf(Math.round(hsva.s * 100)), "%", null, Dimension.PERCENTAGE);
  }

  @Override
  protected String getName() {
    return ColorFunctions.HSV_SATURATION;
  }

}

class HSVValue extends AbstractColorOperationFunction {

  @Override
  protected Expression evaluate(ColorExpression color, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    HSVAValue hsva = toHSVA(color);
    return new NumberExpression(token, Double.valueOf(Math.round(hsva.v * 100)), "%", null, Dimension.PERCENTAGE);
  }

  @Override
  protected String getName() {
    return ColorFunctions.HSV_VALUE;
  }

}

class Red extends AbstractColorOperationFunction {

  @Override
  protected Expression evaluate(ColorExpression color, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    return new NumberExpression(token, Double.valueOf(color.getRed()), "", null, Dimension.NUMBER);
  }

  @Override
  protected String getName() {
    return ColorFunctions.RED;
  }

}

class Green extends AbstractColorOperationFunction {

  @Override
  protected Expression evaluate(ColorExpression color, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    return new NumberExpression(token, Double.valueOf(color.getGreen()), "", null, Dimension.NUMBER);
  }

  @Override
  protected String getName() {
    return ColorFunctions.GREEN;
  }

}

class Blue extends AbstractColorOperationFunction {

  @Override
  protected Expression evaluate(ColorExpression color, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    return new NumberExpression(token, Double.valueOf(color.getBlue()), "", null, Dimension.NUMBER);
  }

  @Override
  protected String getName() {
    return ColorFunctions.BLUE;
  }

}

class Alpha extends CssNameClashMultiParameterFunction {

  @Override
  public Expression evaluate(List<Expression> parameters, ProblemsHandler problemsHandler, FunctionExpression call, Expression evaluatedParameter) {
    ColorExpression color = (ColorExpression) parameters.get(0);
    return new NumberExpression(call.getUnderlyingStructure(), Double.valueOf(color.getAlpha()), "", null, Dimension.NUMBER);
  }

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
    return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.COLOR_EXPRESSION);
  }

  @Override
  protected String getName() {
    return ColorFunctions.ALPHA;
  }

}

class Luma extends AbstractColorOperationFunction {

  @Override
  protected Expression evaluate(ColorExpression color, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    double r = color.getRed() / 255.0, g = color.getGreen() / 255.0, b = color.getBlue() / 255.0;

    r = (r <= 0.03928) ? r / 12.92 : Math.pow(((r + 0.055) / 1.055), 2.4);
    g = (g <= 0.03928) ? g / 12.92 : Math.pow(((g + 0.055) / 1.055), 2.4);
    b = (b <= 0.03928) ? b / 12.92 : Math.pow(((b + 0.055) / 1.055), 2.4);

    double luma =  0.2126 * r + 0.7152 * g + 0.0722 * b;

    return new NumberExpression(token, Double.valueOf(round8(luma * color.getAlpha() * 100)), "%", null, Dimension.PERCENTAGE);
  }

  @Override
  protected String getName() {
    return ColorFunctions.LUMA;
  }

}

class Luminance extends AbstractColorOperationFunction {

  @Override
  protected Expression evaluate(ColorExpression color, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    double luminance = ((0.2126 * (color.getRed() / 255.0) + 0.7152 * (color.getGreen() / 255.0) + 0.0722 * (color.getBlue() / 255.0)) * color.getAlpha() * 100);

    return new NumberExpression(token, Double.valueOf(round8(luminance)), "%", null, Dimension.PERCENTAGE);
  }

  @Override
  protected String getName() {
    return ColorFunctions.LUMINANCE;
  }

}

abstract class AbstractColorOperationFunction extends AbstractColorFunction {

  @Override
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, FunctionExpression functionCall, HiddenTokenAwareTree token) {
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
    return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.COLOR_EXPRESSION);
  }

}

class ColorParameterUtils {
  
  private TypesConversionUtils conversionUtils = new TypesConversionUtils();

  protected boolean isAbsolute(List<Expression> allParameters, int index) {
    boolean isAbsolute = true;
    if (allParameters.size()>index) {
      String kind = conversionUtils.contentToString(allParameters.get(index));
      if (kind !=null && "relative".equals(kind.toLowerCase()))
        isAbsolute = false;
    }
    return isAbsolute;
  }

  public ASTCssNodeType[] modeTypeAcceptableTypes() {
    return conversionUtils.allConvertibleToString();
  }

}

class Saturate extends CssNameClashMultiParameterFunction {
  
  private ColorParameterUtils paramUtils = new ColorParameterUtils();

  @Override
  public Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, FunctionExpression call, Expression evaluatedParameter) {
    Expression firstParam = splitParameters.get(0);
    if (firstParam.getType() != ASTCssNodeType.COLOR_EXPRESSION) {
      UnknownFunction unknownFunction = new UnknownFunction();
      return unknownFunction.evaluate(splitParameters, problemsHandler, call, evaluatedParameter);
    }

    ColorExpression color = (ColorExpression) firstParam;
    NumberExpression amount = (NumberExpression) splitParameters.get(1);
    

    HSLAValue hsla = AbstractColorFunction.toHSLA(color);
    boolean isAbsolute = paramUtils.isAbsolute(splitParameters, 2);
    if (isAbsolute) {
      applyAbsolute(amount, hsla);
    } else { 
      applyRelative(amount, hsla);
    }
    return AbstractColorFunction.hsla(hsla, call.getUnderlyingStructure());
  }

  protected void applyAbsolute(NumberExpression amount, HSLAValue hsla) {
    hsla.s += amount.getValueAsDouble() / 100.0f;
    hsla.s = AbstractColorFunction.clamp(hsla.s);
  }

  protected void applyRelative(NumberExpression amount, HSLAValue hsla) {
    hsla.s += hsla.s * amount.getValueAsDouble() / 100.0f;
    hsla.s = AbstractColorFunction.clamp(hsla.s);
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
      return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.COLOR_EXPRESSION);
    case 1:
      return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.NUMBER);
    case 2:
      return validateParameterTypeReportError(parameter, problemsHandler, paramUtils.modeTypeAcceptableTypes());
      
    }
    return false;
  }

  @Override
  protected String getName() {
    return ColorFunctions.SATURATE;
  }

}

class Desaturate extends AbstractColorHSLAmountFunction {

  @Override
  protected void applyAbsolute(NumberExpression amount, HSLAValue hsla) {
    hsla.s -= amount.getValueAsDouble() / 100.0f;
    hsla.s = clamp(hsla.s);
  }

  @Override
  protected void applyRelative(NumberExpression amount, HSLAValue hsla) {
    hsla.s -= hsla.s * amount.getValueAsDouble() / 100.0f;
    hsla.s = clamp(hsla.s);
  }

  @Override
  protected String getName() {
    return ColorFunctions.DESATURATE;
  }

}

class Lighten extends AbstractColorHSLAmountFunction {

  @Override
  protected void applyAbsolute(NumberExpression amount, HSLAValue hsla) {
    hsla.l += amount.getValueAsDouble() / 100.0f;
    hsla.l = clamp(hsla.l);
  }

  @Override
  protected void applyRelative(NumberExpression amount, HSLAValue hsla) {
    hsla.l += hsla.l * amount.getValueAsDouble() / 100.0f;
    hsla.l = clamp(hsla.l);
  }

  @Override
  protected String getName() {
    return ColorFunctions.LIGHTEN;
  }

}

class Darken extends AbstractColorHSLAmountFunction {

  @Override
  protected void applyAbsolute(NumberExpression amount, HSLAValue hsla) {
    hsla.l -= amount.getValueAsDouble() / 100.0f;
    hsla.l = clamp(hsla.l);
  }

  @Override
  protected void applyRelative(NumberExpression amount, HSLAValue hsla) {
    hsla.l -= hsla.l * amount.getValueAsDouble() / 100.0f;
    hsla.l = clamp(hsla.l);
  }

  @Override
  protected String getName() {
    return ColorFunctions.DARKEN;
  }

}

class FadeIn extends AbstractColorHSLAmountFunction {

  @Override
  protected void applyAbsolute(NumberExpression amount, HSLAValue hsla) {
    hsla.a += amount.getValueAsDouble() / 100.0f;
    hsla.a = clamp(hsla.a);
  }

  @Override
  protected void applyRelative(NumberExpression amount, HSLAValue hsla) {
    hsla.a += hsla.a * amount.getValueAsDouble() / 100.0f;
    hsla.a = clamp(hsla.a);
  }

  @Override
  protected String getName() {
    return ColorFunctions.FADEIN;
  }

}

class FadeOut extends AbstractColorHSLAmountFunction {

  @Override
  protected void applyAbsolute(NumberExpression amount, HSLAValue hsla) {
    hsla.a -= amount.getValueAsDouble() / 100.0f;
    hsla.a = clamp(hsla.a);
  }

  @Override
  protected void applyRelative(NumberExpression amount, HSLAValue hsla) {
    hsla.a -= hsla.a * amount.getValueAsDouble() / 100.0f;
    hsla.a = clamp(hsla.a);
  }

  @Override
  protected String getName() {
    return ColorFunctions.FADEOUT;
  }

}

class Fade extends AbstractColorAmountFunction {

  public Fade() {
    super(false);
  }

  @Override
  protected Expression evaluate(ColorExpression color, NumberExpression amount, boolean isAbsolute, HiddenTokenAwareTree token) {
    HSLAValue hsla = toHSLA(color);

    apply(amount, hsla);

    return hsla(hsla, token);
  }

  protected void apply(NumberExpression amount, HSLAValue hsla) {
    hsla.a = (amount.getValueAsDouble() / 100.0f);
    hsla.a = clamp(hsla.a);
  }

  @Override
  protected String getName() {
    return ColorFunctions.FADE;
  }

}

class Spin extends AbstractColorAmountFunction {

  public Spin() {
    super(false);
  }

  @Override
  protected Expression evaluate(ColorExpression color, NumberExpression amount, boolean isAbsolute, HiddenTokenAwareTree token) {
    HSLAValue hsla = toHSLA(color);

    apply(amount, hsla);

    return hsla(hsla, token);
  }

  protected void apply(NumberExpression amount, HSLAValue hsla) {
    double hue = ((hsla.h + amount.getValueAsDouble()) % 360);
    hsla.h = hue < 0 ? 360 + hue : hue;
  }

  @Override
  protected String getName() {
    return ColorFunctions.SPIN;
  }

}

//
// Copyright (c) 2006-2009 Hampton Catlin, Nathan Weizenbaum, and Chris Eppstein
// http://sass-lang.com
//
class Mix extends AbstractColorFunction {

  @Override
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, FunctionExpression functionCall, HiddenTokenAwareTree token) {
    ColorExpression color1 = (ColorExpression) splitParameters.get(0);
    ColorExpression color2 = (ColorExpression) splitParameters.get(1);
    NumberExpression weight = splitParameters.size() > 2 ? (NumberExpression) splitParameters.get(2) : null;

    if (weight == null) {
      weight = new NumberExpression(token, Double.valueOf(50), "%", null, Dimension.PERCENTAGE);
    }

    return mix(color1, color2, weight, token);
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
      return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.COLOR_EXPRESSION);
    case 2:
      return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.NUMBER);
    }
    return false;
  }

  @Override
  protected String getName() {
    return ColorFunctions.MIX;
  }

}

class Greyscale extends AbstractColorOperationFunction {

  @Override
  protected Expression evaluate(ColorExpression color, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    HSLAValue hsla = toHSLA(color);
    hsla.s = 0;
    return hsla(hsla, token);
  }

  @Override
  protected String getName() {
    return ColorFunctions.GREYSCALE;
  }

}

class Contrast extends CssNameClashMultiParameterFunction {

  @Override
  public Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, FunctionExpression call, Expression evaluatedParameter) {
    /* Contrast needs to support an invalid first parameter to succeed in less.js test cases.
     * I think this is in order to support filter: rules so may not be a good idea.
     * We return null to ColorFunctions which will in turn return the input, so in effect we change
     * nothing.
     */
    if (splitParameters.get(0).getType() != ASTCssNodeType.COLOR_EXPRESSION) {
      UnknownFunction unknownFunction = new UnknownFunction();
      return unknownFunction.evaluate(splitParameters, problemsHandler, call, evaluatedParameter);
    }

    HiddenTokenAwareTree token = call.getUnderlyingStructure();

    ColorExpression color = (ColorExpression) splitParameters.get(0);
    ColorExpression dark = (ColorExpression) (splitParameters.size() > 1 ? splitParameters.get(1) : new ColorExpression(token, 0, 0, 0));
    ColorExpression light = (ColorExpression) (splitParameters.size() > 2 ? splitParameters.get(2) : new ColorExpression(token, 255, 255, 255));
    NumberExpression threshold = (NumberExpression) (splitParameters.size() > 3 ? splitParameters.get(3) : new NumberExpression(token, 43.0, "%", null, Dimension.PERCENTAGE));
    double thresholdValue = AbstractColorFunction.number(threshold);

    if (((0.2126 * (color.getRed() / 255) + 0.7152 * (color.getGreen() / 255) + 0.0722 * (color.getBlue() / 255)) * color.getAlpha()) < thresholdValue) {
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
      /* Contrast needs to support an invalid first parameter to succeed in less.js test cases.
       * I think this is in order to support filter: rules so may not be a good idea.
       */
      return true;
    case 1:
    case 2:
      return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.COLOR_EXPRESSION);
    case 3:
      return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.NUMBER);
    }
    return false;
  }

  @Override
  protected String getName() {
    return ColorFunctions.CONTRAST;
  }

}

class Multiply extends AbstractSimpleColorBlendFunction {

  @Override
  protected double evaluateNormalized(double a, double b) {
    return a * b ;
  }

  @Override
  protected String getName() {
    return ColorFunctions.MULTIPLY;
  }

}

class Screen extends AbstractSimpleColorBlendFunction {

  @Override
  protected double evaluateNormalized(double a, double b) {
    return 1 - (1 - a) * (1 - b);
  }

  @Override
  protected String getName() {
    return ColorFunctions.SCREEN;
  }

}

class Overlay extends AbstractSimpleColorBlendFunction {

  @Override
  protected double evaluateNormalized(double a, double b) {
    return a < 0.5 ? 2 * a * b  : 1 - 2 * (1 - a) * (1 - b);
  }

  @Override
  protected String getName() {
    return ColorFunctions.OVERLAY;
  }

}

class Softlight extends AbstractSimpleColorBlendFunction {

  @Override
  protected double evaluateNormalized(double cb, double cs) {
    double d = 1, e = cb;
    if (cs > 0.5) {
        e = 1;
        d = (cb > 0.25) ? Math.sqrt(cb)
            : ((16 * cb - 12) * cb + 4) * cb;
    }
    return cb - (1 - 2 * cs) * e * (d - cb);
  }

  @Override
  protected String getName() {
    return ColorFunctions.SOFTLIGHT;
  }

}

class Hardlight extends AbstractSimpleColorBlendFunction {

  @Override
  protected double evaluateNormalized(double a, double b) {
    return b < 0.5 ? 2 * b * a  : 1 - 2 * (1 - b) * (1 - a);
  }

  @Override
  protected String getName() {
    return ColorFunctions.HARDLIGHT;
  }

}

class Difference extends AbstractSimpleColorBlendFunction {

  @Override
  protected double evaluateNormalized(double a, double b) {
    return Math.abs(a - b);
  }

  @Override
  protected String getName() {
    return ColorFunctions.DIFFERENCE;
  }

}

class Exclusion extends AbstractSimpleColorBlendFunction {

  @Override
  protected double evaluateNormalized(double a, double b) {
    return a + b * (1 - a - a);
  }

  @Override
  protected String getName() {
    return ColorFunctions.EXCLUSION;
  }

}

class Average extends AbstractSimpleColorBlendFunction {

  @Override
  protected double evaluateNormalized(double a, double b) {
    return (a + b) / 2;
  }

  @Override
  protected String getName() {
    return ColorFunctions.AVERAGE;
  }

}

class Negation extends AbstractSimpleColorBlendFunction {

  @Override
  protected double evaluateNormalized(double a, double b) {
    return 1 - Math.abs(1 - b - a);
  }

  @Override
  protected String getName() {
    return ColorFunctions.NEGATION;
  }

}

class Tint extends AbstractColorAmountFunction {

  public Tint() {
    super(false);
  }

  @Override
  protected Expression evaluate(ColorExpression color, NumberExpression amount, boolean isAbsolute, HiddenTokenAwareTree token) {
    return mix(rgb(255, 255, 255, token), color, amount, token);
  }

  @Override
  protected String getName() {
    return ColorFunctions.TINT;
  }

}

class Shade extends AbstractColorAmountFunction {

  public Shade() {
    super(false);
  }

  @Override
  protected Expression evaluate(ColorExpression color, NumberExpression amount, boolean isAbsolute, HiddenTokenAwareTree token) {
    return mix(rgb(0, 0, 0, token), color, amount, token);
  }

  @Override
  protected String getName() {
    return ColorFunctions.SHADE;
  }

}

abstract class AbstractSimpleColorBlendFunction extends AbstractColorBlendFunction {

  @Override
  protected Expression evaluate(ColorExpression color1, ColorExpression color2, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    double cbRed = color1.getRed()/255.0;
    double csRed = color2.getRed()/255.0;
    double resultRed = evaluateNormalized(cbRed, csRed);
    double cbGreen = color1.getGreen()/255.0;
    double csGreen = color2.getGreen()/255.0;
    double resultGreen = evaluateNormalized(cbGreen, csGreen);
    double cbBlue = color1.getBlue()/255.0;
    double csBlue = color2.getBlue()/255.0;
    double resultBlue = evaluateNormalized(cbBlue, csBlue);
    
    if (!color1.hasAlpha() && !color2.hasAlpha()) {
      return rgb(resultRed * 255.0, resultGreen * 255.0, resultBlue * 255.0, token);
    }
    
    double ab = color1.getAlpha();
    double as = color2.getAlpha();
    
    double resultAlpha = as + ab * (1 - as);
    resultRed = addAlpha(cbRed, ab, csRed, as, resultRed, resultAlpha);
    resultGreen = addAlpha(cbGreen, ab, csGreen, as, resultGreen, resultAlpha);
    resultBlue = addAlpha(cbBlue, ab, csBlue, as, resultBlue, resultAlpha);

    return rgba(resultRed * 255.0, resultGreen * 255.0, resultBlue * 255.0, resultAlpha, token);
  }
  
  

  private double addAlpha(double cb, double ab, double cs, double as, double cr, double ar) {
    return (as * cs + ab * (cb -
        as * (cb + cs - cr))) / ar;
  }



  protected abstract double evaluateNormalized(double a, double b);

}

abstract class AbstractColorBlendFunction extends AbstractColorFunction {

  @Override
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, FunctionExpression functionCall, HiddenTokenAwareTree token) {
    return evaluate((ColorExpression) splitParameters.get(0), (ColorExpression) splitParameters.get(1), problemsHandler, token);
  }

  protected abstract Expression evaluate(ColorExpression color1, ColorExpression color2, ProblemsHandler problemsHandler, HiddenTokenAwareTree token);

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
    return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.COLOR_EXPRESSION);
  }

}

abstract class AbstractColorFunction extends CatchAllMultiParameterFunction {
  
  static double round8(double value) {
    double rounding = 100000000.0;
    return Math.round(value*rounding)/rounding;
  }

  static double clamp(double val) {
    return Math.min(1, Math.max(0, val));
  }

  static ColorExpression rgb(double r, double g, double b, HiddenTokenAwareTree token) {
    return new ColorExpression(token, r, g, b);
  }

  static ColorExpression rgba(double r, double g, double b, double a, HiddenTokenAwareTree token) {
    return new ColorExpression.ColorWithAlphaExpression(token, r, g, b, a);
  }

  static ColorExpression hsla(HSLAValue hsla, HiddenTokenAwareTree token) {
    double h = (hsla.h % 360.0) / 360.0, s = hsla.s, l = hsla.l, a = hsla.a;

    double m2 = l <= 0.5 ? l * (s + 1) : l + s - l * s;
    double m1 = l * 2 - m2;

    return rgba(hue(h + 1.0 / 3.0, m1, m2) * 255, hue(h, m1, m2) * 255, hue(h - 1.0 / 3.0, m1, m2) * 255, a, token);
  }

  private static double hue(double h, double m1, double m2) {
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

  static final int[][] hsvaPerm = new int[][] { new int[] { 0, 3, 1 }, new int[] { 2, 0, 1 }, new int[] { 1, 0, 3 }, new int[] { 1, 2, 0 }, new int[] { 3, 1, 0 }, new int[] { 0, 1, 2 } };

  static ColorExpression hsva(double h, double s, double v, double a, HiddenTokenAwareTree token) {
    h = ((h % 360) / 360) * 360;

    int i = (int) Math.floor((h / 60) % 6);
    double f = (h / 60) - i;

    double[] vs = new double[] { v, v * (1 - s), v * (1 - f * s), v * (1 - (1 - f) * s) };

    return rgba(vs[hsvaPerm[i][0]] * 255, vs[hsvaPerm[i][1]] * 255, vs[hsvaPerm[i][2]] * 255, a, token);
  }

  /**
   * Mix
   * @param color1
   * @param color2
   * @param weight number 0-100.
   * @return
   */
  protected static Expression mix(ColorExpression color1, ColorExpression color2, NumberExpression weight, HiddenTokenAwareTree token) {
    double p = weight.getValueAsDouble() / 100.0;
    double w = p * 2 - 1;
    double a = color1.getAlpha() - color2.getAlpha();

    double w1 = (((w * a == -1) ? w : (w + a) / (1 + w * a)) + 1) / 2.0;
    double w2 = 1 - w1;

    double red = color1.getRed() * w1 + color2.getRed() * w2;
    double green = color1.getGreen() * w1 + color2.getGreen() * w2;
    double blue = color1.getBlue() * w1 + color2.getBlue() * w2;

    if (color1.hasAlpha() || color2.hasAlpha()) {
      double alpha = color1.getAlpha() * p + color2.getAlpha() * (1 - p);
      return rgba(red, green, blue, alpha, token);
    } else {
      return rgb(red, green, blue, token);
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

  static HSVAValue toHSVA(ColorExpression color) {
    double r = color.getRed() / 255.0, g = color.getGreen() / 255.0, b = color.getBlue() / 255.0, a = color.getAlpha();

    double max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b));
    double h, s, v = max, d = max - min;

    if (max == 0) {
      s = 0;
    } else {
      s = d / max;
    }

    if (max == min) {
      h = 0;
    } else {
      if (max == r) {
        h = (g - b) / d + (g < b ? 6 : 0);
      } else if (max == g) {
        h = (b - r) / d + 2;
      } else { //if (max == b) 
        h = (r - g) / d + 4;
      }
      h = h / 6;
    }

    return new HSVAValue(h * 360, s, v, a);
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

class HSVAValue {
  public double h, s, v, a;

  public HSVAValue() {
    super();
  }

  public HSVAValue(double h, double s, double v, double a) {
    super();
    this.h = h;
    this.s = s;
    this.v = v;
    this.a = a;
  }

  public HSVAValue(double h, double s, double v) {
    super();
    this.h = h;
    this.s = s;
    this.v = v;
    this.a = 1.0f;
  }
}

abstract class AbstractColorAmountFunction extends AbstractColorFunction {
  
  private final ColorParameterUtils paramUtils = new ColorParameterUtils();
  private final boolean supportsRelativeOption;

  public AbstractColorAmountFunction(boolean supportsRelativeOption) {
    this.supportsRelativeOption = supportsRelativeOption;
  }

  @Override
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, FunctionExpression functionCall, HiddenTokenAwareTree token) {
    ColorExpression color = (ColorExpression) splitParameters.get(0);
    NumberExpression amount = (NumberExpression) splitParameters.get(1);

    return evaluate(color, amount, paramUtils.isAbsolute(splitParameters, 2), token);
  }

  protected abstract Expression evaluate(ColorExpression color, NumberExpression amount, boolean isAbsolute, HiddenTokenAwareTree token);

  @Override
  protected int getMinParameters() {
    return 2;
  }

  @Override
  protected int getMaxParameters() {
    return supportsRelativeOption? 3 : 2;
  }

  @Override
  protected boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler) {
    switch (position) {
    case 0:
      return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.COLOR_EXPRESSION);
    case 1:
      return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.NUMBER);
    case 2:
      return validateParameterTypeReportError(parameter, problemsHandler, paramUtils.modeTypeAcceptableTypes());
    }
    return false;
  }

}

abstract class AbstractColorHSLAmountFunction extends AbstractColorAmountFunction {

  public AbstractColorHSLAmountFunction() {
    super(true);
  }

  @Override
  protected Expression evaluate(ColorExpression color, NumberExpression amount, boolean isAbsolute, HiddenTokenAwareTree token) {
    HSLAValue hsla = toHSLA(color);

    if (isAbsolute) {
      applyAbsolute(amount, hsla);
    } else {
      applyRelative(amount, hsla);
    }

    return hsla(hsla, token);
  }

  /**
   * Apply the amount to the given hsla array.
   * 
   * @param amount
   * @param hsla
   */
  protected abstract void applyAbsolute(NumberExpression amount, HSLAValue hsla);

  /**
   * Apply the amount to the given hsla array.
   * 
   * @param amount
   * @param hsla
   */
  protected abstract void applyRelative(NumberExpression amount, HSLAValue hsla);

}
