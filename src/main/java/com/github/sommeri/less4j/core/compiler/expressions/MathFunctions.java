package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FaultyExpression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.ListExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression.Dimension;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class MathFunctions extends BuiltInFunctionsPack {

  protected static final String PERCENTAGE = "percentage";
  protected static final String ROUND = "round";
  protected static final String MIN = "min";
  protected static final String MAX = "max";
  protected static final String LENGTH = "length";
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
    FUNCTIONS.put(MIN, new Min());
    FUNCTIONS.put(MAX, new Max());
    FUNCTIONS.put(LENGTH, new Length());
    
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
      problemsHandler.wrongNumberOfArgumentsToFunctionMin(call.getParameter(), call.getName(), 1);

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
      problemsHandler.wrongNumberOfArgumentsToFunctionMin(call.getParameter(), call.getName(), 1);

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
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, FunctionExpression functionCall, HiddenTokenAwareTree token) {
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
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, FunctionExpression functionCall, HiddenTokenAwareTree token) {
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
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, FunctionExpression functionCall, HiddenTokenAwareTree parentToken) {
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

abstract class MinMax extends AbtractMultiParameterMathFunction {

  @Override
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, FunctionExpression functionCall, HiddenTokenAwareTree token) {
    Iterator<Expression> expanded = expandLists(splitParameters).iterator();
    Expression next = expanded.next();
    while (expanded.hasNext() && next.getType()!=ASTCssNodeType.NUMBER) {
      next = expanded.next();
    }
    
    if (next.getType()!=ASTCssNodeType.NUMBER) {
      return new FunctionExpression(token, getName(), null);
    }
    
    NumberExpression result = (NumberExpression) next;
    while (expanded.hasNext()) {
      next = expanded.next();
      if (next.getType()==ASTCssNodeType.NUMBER) {
        NumberExpression nextNum = (NumberExpression) next;
        if (!canCompare(result, nextNum)) {
          problemsHandler.errorIncompatibleTypesCompared(functionCall, result.getSuffix(), nextNum.getSuffix());
          return new FaultyExpression(functionCall);
        }
        
        result = compareCompatible(result, nextNum); 
      }
    }
    
    return result;
  }

  protected abstract NumberExpression compareCompatible(NumberExpression first, NumberExpression second);

  private boolean canCompare(NumberExpression first, NumberExpression second) {
    return first.getDimension() == second.getDimension() || first.getDimension()==Dimension.NUMBER || second.getDimension()==Dimension.NUMBER;
  }

  private List<Expression> expandLists(List<Expression> splitParameters) {
    List<Expression> result = new ArrayList<Expression>();
    for (Expression expression : splitParameters) {
      if (expression instanceof ListExpression) {
        ListExpression list = (ListExpression) expression;
        result.addAll(expandLists(list.getExpressions()));
      } else {
        result.add(expression);
      }
    }
    return result;
  }

  @Override
  protected int getMaxParameters() {
    return Integer.MAX_VALUE;
  }

  @Override
  protected boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler) {
//    boolean isCorrect = validateParameterTypeDoNotReport(parameter, problemsHandler, ASTCssNodeType.LIST_EXPRESSION, ASTCssNodeType.NUMBER);
//    if (isCorrect) {
//      lklk
//    }
    return true;
  }

  @Override
  protected int getMinParameters() {
    return 1;
  }

}

class Pi extends AbstractFunction {

  @Override
  public Expression evaluate(List<Expression> parameters, ProblemsHandler problemsHandler, FunctionExpression call, Expression evaluatedParameter) {
    return new NumberExpression(call.getUnderlyingStructure(), Math.PI, "", null, Dimension.NUMBER);
  }
  
}

class Min extends MinMax {

  @Override
  protected String getName() {
    return MathFunctions.MIN;
  }

  protected NumberExpression compareCompatible(NumberExpression first, NumberExpression second) {
    NumberExpression secondConverted = second.convertTo(first.getSuffix());
    if (secondConverted.getValueAsDouble()<first.getValueAsDouble())
      return second;
    
    return first;
  }

}

class Max extends MinMax {

  @Override
  protected String getName() {
    return MathFunctions.MAX;
  }

  protected NumberExpression compareCompatible(NumberExpression first, NumberExpression second) {
    NumberExpression secondConverted = second.convertTo(first.getSuffix());
    if (secondConverted.getValueAsDouble()>first.getValueAsDouble())
      return second;
    
    return first;
  }

}

class Length extends AbtractMultiParameterMathFunction {

  @Override
  protected String getName() {
    return MathFunctions.LENGTH;
  }

  @Override
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, FunctionExpression call, HiddenTokenAwareTree token) {
    Expression expression = splitParameters.get(0);
    if (expression.getType()!=ASTCssNodeType.LIST_EXPRESSION)
      return new NumberExpression(call.getUnderlyingStructure(), Double.valueOf(1), "", null, Dimension.NUMBER);
    
    ListExpression list = (ListExpression) expression;
    int length = list.getExpressions().size();
    return new NumberExpression(call.getUnderlyingStructure(), Double.valueOf(length), "", null, Dimension.NUMBER);
  }

  @Override
  protected boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler) {
    return true;
  }
  
  @Override
  protected int getMinParameters() {
    return 1;
  }

  @Override
  protected int getMaxParameters() {
    return Integer.MAX_VALUE;
  }

}

