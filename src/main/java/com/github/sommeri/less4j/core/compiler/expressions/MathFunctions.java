package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.HashMap;
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

public class MathFunctions implements FunctionsPackage {

  protected static final String PERCENTAGE = "percentage";
  protected static final String ROUND = "round";
  protected static final String FLOOR = "floor";
  protected static final String CEIL = "ceil";

  private static Map<String, Function> FUNCTIONS = new HashMap<String, Function>();
  static {
    FUNCTIONS.put(PERCENTAGE, new Percentage());
    FUNCTIONS.put(FLOOR, new Floor());
    FUNCTIONS.put(CEIL, new Ceil());
    FUNCTIONS.put(ROUND, new Round());
  }

  private final ProblemsHandler problemsHandler;

  public MathFunctions(ProblemsHandler problemsHandler) {
    this.problemsHandler = problemsHandler;
  }

  /* (non-Javadoc)
   * @see com.github.sommeri.less4j.core.compiler.expressions.FunctionsPackage#canEvaluate(com.github.sommeri.less4j.core.ast.FunctionExpression, com.github.sommeri.less4j.core.ast.Expression)
   */
  @Override
  public boolean canEvaluate(FunctionExpression input, Expression parameters) {
    return FUNCTIONS.containsKey(input.getName());
  }
  
  /* (non-Javadoc)
   * @see com.github.sommeri.less4j.core.compiler.expressions.FunctionsPackage#evaluate(com.github.sommeri.less4j.core.ast.FunctionExpression, com.github.sommeri.less4j.core.ast.Expression)
   */
  @Override
  public Expression evaluate(FunctionExpression input, Expression parameters) {
    if (!canEvaluate(input, parameters))
      return input;

    Function function = FUNCTIONS.get(input.getName());
    return function.evaluate(parameters, problemsHandler);
  }

}

class Percentage implements Function {

  private final ConversionUtils utils = new ConversionUtils();

  @Override
  public Expression evaluate(Expression parameter, ProblemsHandler problemsHandler) {
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

abstract class RoundingFunction implements Function {
  
  @Override
  public final Expression evaluate(Expression iParameter, ProblemsHandler problemsHandler) {
    if (iParameter.getType() != ASTCssNodeType.NUMBER) {
      problemsHandler.mathFunctionParameterNotANumber(getName(), iParameter);
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

  protected abstract String getName();
  protected abstract Expression calc(HiddenTokenAwareTree parentToken, Double oValue, String suffix, Dimension dimension);

}

class Floor extends RoundingFunction {

  @Override
  protected Expression calc(HiddenTokenAwareTree parentToken, Double oValue, String suffix, Dimension dimension) {
    return new NumberExpression(parentToken, Math.floor(oValue), suffix, null, dimension);
  }

  @Override
  protected String getName() {
    return MathFunctions.FLOOR;
  }
}

class Ceil extends RoundingFunction {

  @Override
  protected NumberExpression calc(HiddenTokenAwareTree parentToken, Double oValue, String suffix, Dimension dimension) {
    return new NumberExpression(parentToken, Math.ceil(oValue), suffix, null, dimension);
  }

  @Override
  protected String getName() {
    return MathFunctions.CEIL;
  }
}

class Round extends RoundingFunction {

  @Override
  protected Expression calc(HiddenTokenAwareTree parentToken, Double oValue, String suffix, Dimension dimension) {
    return new NumberExpression(parentToken, (double)Math.round(oValue), suffix, null, dimension);
  }

  @Override
  protected String getName() {
    return MathFunctions.ROUND;
  }
}

