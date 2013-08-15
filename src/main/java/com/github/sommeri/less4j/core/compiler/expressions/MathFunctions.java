package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FaultyExpression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression.Dimension;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class MathFunctions extends BuiltInFunctionsPack {

  protected static final String PERCENTAGE = "percentage";
  protected static final String ROUND = "round";
  protected static final String FLOOR = "floor";
  protected static final String CEIL = "ceil";
  protected static final String SQRT = "sqrt";
  protected static final String ABS = "abs";
  protected static final String TAN = "tan";
  protected static final String SIN = "sin";
  protected static final String COS = "cos";
  protected static final String MOD = "mod";
  protected static final String POW = "pow";
  protected static final String ATAN = "atan";
  protected static final String ASIN = "asin";
  protected static final String ACOS = "acos";
  protected static final String PI = "pi";

  private static Map<String, Function> FUNCTIONS = new HashMap<String, Function>();
  static {
    FUNCTIONS.put(PERCENTAGE, new Percentage());
    FUNCTIONS.put(FLOOR, new Floor());
    FUNCTIONS.put(CEIL, new Ceil());
    FUNCTIONS.put(ROUND, new Round());
    FUNCTIONS.put(SQRT, new Sqrt());
    FUNCTIONS.put(ABS, new Abs());
    FUNCTIONS.put(TAN, new Tan());
    FUNCTIONS.put(SIN, new Sin());
    FUNCTIONS.put(COS, new Cos());
    FUNCTIONS.put(MOD, new Mod());
    FUNCTIONS.put(POW, new Pow());
    FUNCTIONS.put(ATAN, new Atan());
    FUNCTIONS.put(ASIN, new Asin());
    FUNCTIONS.put(ACOS, new Acos());
    FUNCTIONS.put(PI, new Pi());
  }

  public MathFunctions(ProblemsHandler problemsHandler) {
    super(problemsHandler);
  }

  @Override
  protected Map<String, Function> getFunctions() {
    return FUNCTIONS;
  }

}

class Percentage extends AbstractFunction {

  private final TypesConversionUtils utils = new TypesConversionUtils();

  @Override
  public Expression evaluate(List<Expression> parameters, ProblemsHandler problemsHandler, FunctionExpression call, Expression evaluatedParameter) {
    if (parameters.size()>1)
      problemsHandler.wrongNumberOfArgumentsToFunction(call.getParameter(), call.getName(), 1);

    Expression parameter = parameters.get(0);
    if (parameter.getType() == ASTCssNodeType.NUMBER) {
      return evaluate((NumberExpression) parameter);
    }

    if (parameter.getType() == ASTCssNodeType.STRING_EXPRESSION) {
      return evaluate((CssString) parameter, problemsHandler);
    }

    problemsHandler.mathFunctionParameterNotANumberWarn(MathFunctions.PERCENTAGE, parameter);
    return createResult(Double.NaN, parameter.getUnderlyingStructure());
  }

  private Expression evaluate(CssString parameter, ProblemsHandler problemsHandler) {
    Double value = utils.toDouble(parameter);
    if (value.isNaN())
      problemsHandler.mathFunctionParameterNotANumberWarn(MathFunctions.PERCENTAGE, parameter);

    return createResult(value, parameter.getUnderlyingStructure());
  }

  private Expression evaluate(NumberExpression parameter) {
    return createResult(parameter.getValueAsDouble(), parameter.getUnderlyingStructure());
  }

  private Expression createResult(Double originalValue, HiddenTokenAwareTree parentToken) {
    Double value = originalValue * 100.0;

    return new NumberExpression(parentToken, value, "%", null, Dimension.PERCENTAGE);
  }

}

abstract class AbstractSingleValueMathFunction implements Function {

  public boolean acceptsParameters(List<Expression> parameters) {
    return true;
  }

  @Override
  public final Expression evaluate(List<Expression> parameters, ProblemsHandler problemsHandler, FunctionExpression call, Expression evaluatedParameter) {
    if (parameters.size()>1)
      problemsHandler.wrongNumberOfArgumentsToFunction(call.getParameter(), call.getName(), 1);

    Expression iParameter = parameters.get(0);
    if (iParameter.getType() != ASTCssNodeType.NUMBER) {
      problemsHandler.wrongArgumentTypeToFunction(iParameter, getName(), iParameter.getType(), ASTCssNodeType.NUMBER);
      return new FaultyExpression(iParameter);
    }

    NumberExpression parameter = (NumberExpression) iParameter;
    HiddenTokenAwareTree parentToken = parameter.getUnderlyingStructure();
    Double oValue = parameter.getValueAsDouble();
    String suffix = parameter.getSuffix();
    Dimension dimension = parameter.getDimension();
    
    if (oValue.isInfinite() || oValue.isNaN())
      return new NumberExpression(parentToken, oValue, suffix, null, dimension);

    return calc(parentToken, oValue, suffix, dimension);
  }
  
  protected Expression calc(HiddenTokenAwareTree parentToken, Double oValue, String suffix, Dimension dimension) {
    return new NumberExpression(parentToken, calc(oValue, suffix, dimension), resultSuffix(suffix, dimension),
	null, resultDimension(suffix, dimension));
  }
  
  protected String resultSuffix(String suffix, Dimension dimension) {
    return suffix;
  }
  
  protected Dimension resultDimension(String suffix, Dimension dimension) {
    return dimension;
  }

  protected abstract String getName();
  
  protected abstract double calc(double d, String suffix, Dimension dimension);

}

class Floor extends AbstractSingleValueMathFunction {

  @Override
  protected double calc(double d, String suffix, Dimension dimension) {
    return Math.floor(d);
  }

  @Override
  protected String getName() {
    return MathFunctions.FLOOR;
  }
}

class Ceil extends AbstractSingleValueMathFunction {

  @Override
  protected double calc(double d, String suffix, Dimension dimension) {
    return Math.ceil(d);
  }

  @Override
  protected String getName() {
    return MathFunctions.CEIL;
  }
}

class Sqrt extends AbstractSingleValueMathFunction {

  @Override
  protected String getName() {
    return MathFunctions.SQRT;
  }
  
  @Override
  protected double calc(double d, String suffix, Dimension dimension) {
    return Math.sqrt(d);
  }
  
}

class Abs extends AbstractSingleValueMathFunction {

  @Override
  protected String getName() {
    return MathFunctions.ABS;
  }
  
  @Override
  protected double calc(double d, String suffix, Dimension dimension) {
    return Math.abs(d);
  }
  
}

class Tan extends AbstractSingleValueMathFunction {

  @Override
  protected String getName() {
    return MathFunctions.TAN;
  }
  
  @Override
  protected double calc(double d, String suffix, Dimension dimension) {
    if (suffix.equalsIgnoreCase("deg")) {
      d = Math.toRadians(d);
    } else if (suffix.equalsIgnoreCase("grad")) {
      d *= Math.PI / 200;
    }
    return Math.tan(d);
  }

  @Override
  protected String resultSuffix(String suffix, Dimension dimension) {
    return "";
  }

  @Override
  protected Dimension resultDimension(String suffix, Dimension dimension) {
    return Dimension.NUMBER;
  }
  
}

class Atan extends AbstractSingleValueMathFunction {

  @Override
  protected String getName() {
    return MathFunctions.ATAN;
  }
  
  @Override
  protected double calc(double d, String suffix, Dimension dimension) {
    return Math.atan(d);
  }

  @Override
  protected String resultSuffix(String suffix, Dimension dimension) {
    return "rad";
  }

  @Override
  protected Dimension resultDimension(String suffix, Dimension dimension) {
    return Dimension.ANGLE;
  }
  
}

class Sin extends AbstractSingleValueMathFunction {

  @Override
  protected String getName() {
    return MathFunctions.SIN;
  }
  
  @Override
  protected double calc(double d, String suffix, Dimension dimension) {
    if (suffix.equalsIgnoreCase("deg")) {
      d = Math.toRadians(d);
    } else if (suffix.equalsIgnoreCase("grad")) {
      d *= Math.PI / 200;
    }
    return Math.sin(d);
  }

  @Override
  protected String resultSuffix(String suffix, Dimension dimension) {
    return "";
  }

  @Override
  protected Dimension resultDimension(String suffix, Dimension dimension) {
    return Dimension.NUMBER;
  }
  
}

class Asin extends AbstractSingleValueMathFunction {

  @Override
  protected String getName() {
    return MathFunctions.ASIN;
  }
  
  @Override
  protected double calc(double d, String suffix, Dimension dimension) {
    return Math.asin(d);
  }

  @Override
  protected String resultSuffix(String suffix, Dimension dimension) {
    return "rad";
  }

  @Override
  protected Dimension resultDimension(String suffix, Dimension dimension) {
    return Dimension.ANGLE;
  }
  
}

class Cos extends AbstractSingleValueMathFunction {

  @Override
  protected String getName() {
    return MathFunctions.COS;
  }
  
  @Override
  protected double calc(double d, String suffix, Dimension dimension) {
    if (suffix.equalsIgnoreCase("deg")) {
      d = Math.toRadians(d);
    } else if (suffix.equalsIgnoreCase("grad")) {
      d *= Math.PI / 200;
    }
    return Math.cos(d);
  }

  @Override
  protected String resultSuffix(String suffix, Dimension dimension) {
    return "";
  }

  @Override
  protected Dimension resultDimension(String suffix, Dimension dimension) {
    return Dimension.NUMBER;
  }
  
}

class Acos extends AbstractSingleValueMathFunction {

  @Override
  protected String getName() {
    return MathFunctions.ACOS;
  }
  
  @Override
  protected double calc(double d, String suffix, Dimension dimension) {
    return Math.acos(d);
  }

  @Override
  protected String resultSuffix(String suffix, Dimension dimension) {
    return "rad";
  }

  @Override
  protected Dimension resultDimension(String suffix, Dimension dimension) {
    return Dimension.ANGLE;
  }
  
}

abstract class AbtractMultiParameterMathFunction extends CatchAllMultiParameterFunction {

  @Override
  protected boolean validateParameterTypeReportError(Expression parameter, ProblemsHandler problemsHandler, ASTCssNodeType... expected) {
      return super.validateParameterTypeReportError(parameter, problemsHandler, expected);
  }
  
}

class Mod extends AbtractMultiParameterMathFunction {

  @Override
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    NumberExpression a = (NumberExpression) splitParameters.get(0);
    NumberExpression b = (NumberExpression) splitParameters.get(1);
    return new NumberExpression(token, a.getValueAsDouble() % b.getValueAsDouble(), a.getSuffix(), null, a.getDimension());
  }

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
    return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.NUMBER);
  }

  @Override
  protected String getName() {
    return MathFunctions.MOD;
  }
  
}

class Pow extends AbstractTwoValueMathFunction {

  @Override
  protected Expression evaluate(NumberExpression a, NumberExpression b, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    return new NumberExpression(token, Math.pow(a.getValueAsDouble(), b.getValueAsDouble()), a.getSuffix(), null, a.getDimension());
  }

  @Override
  protected String getName() {
    return MathFunctions.POW;
  }
  
}

abstract class AbstractTwoValueMathFunction extends AbtractMultiParameterMathFunction {

  @Override
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    return evaluate((NumberExpression)splitParameters.get(0), (NumberExpression)splitParameters.get(1), problemsHandler, token);
  }

  protected abstract Expression evaluate(NumberExpression a, NumberExpression b, ProblemsHandler problemsHandler, HiddenTokenAwareTree token);

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
    return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.NUMBER);
  }
  
}

class Round extends AbtractMultiParameterMathFunction {

  @Override
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, HiddenTokenAwareTree parentToken) {
    NumberExpression parameter = (NumberExpression) splitParameters.get(0);
    Double oValue = parameter.getValueAsDouble();
    String suffix = parameter.getSuffix();
    Dimension dimension = parameter.getDimension();
    
    if (oValue.isInfinite() || oValue.isNaN())
      return new NumberExpression(parentToken, oValue, suffix, null, dimension);

    NumberExpression fraction = (NumberExpression) (splitParameters.size() > 1 ? splitParameters.get(1) : null);
    if (fraction != null) {
      double pow = Math.pow(10, fraction.getValueAsDouble());
      oValue = Math.round(oValue * pow) / pow;
    } else {
      oValue = (double) Math.round(oValue);
    }
    return new NumberExpression(parentToken, oValue, suffix, null, dimension);
  }

  @Override
  protected int getMinParameters() {
    return 1;
  }

  @Override
  protected int getMaxParameters() {
    return 2;
  }

  @Override
  protected boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler) {
    return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.NUMBER);
  }

  @Override
  protected String getName() {
    return MathFunctions.ROUND;
  }

}

class Pi extends AbstractFunction {

  @Override
  public Expression evaluate(List<Expression> parameters, ProblemsHandler problemsHandler, FunctionExpression call, Expression evaluatedParameter) {
    return new NumberExpression(call.getUnderlyingStructure(), Math.PI, "", null, Dimension.NUMBER);
  }
  
}
