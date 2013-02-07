package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression.Dimension;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class TypeFunctions implements FunctionsPackage {

  protected static final String ISCOLOR = "iscolor";
  protected static final String ISKEYWORD = "iskeyword";
  protected static final String ISNUMBER = "isnumber";
  protected static final String ISSTRING = "isstring";
  protected static final String ISPIXEL = "ispixel";
  protected static final String ISPERCENTAGE = "ispercentage";
  protected static final String ISEM = "isem";

  private static Map<String, Function> FUNCTIONS = new HashMap<String, Function>();
  static {
    FUNCTIONS.put(ISCOLOR, new IsColor());
    FUNCTIONS.put(ISKEYWORD, new IsKeyword());
    FUNCTIONS.put(ISNUMBER, new IsNumber());
    FUNCTIONS.put(ISSTRING, new IsString());
    FUNCTIONS.put(ISPIXEL, new IsPixel());
    FUNCTIONS.put(ISPERCENTAGE, new IsPercentage());
    FUNCTIONS.put(ISEM, new IsEm());
  }

  private final ProblemsHandler problemsHandler;

  public TypeFunctions(ProblemsHandler problemsHandler) {
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
  public boolean canEvaluate(FunctionExpression input, List<Expression> parameters) {
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
  public Expression evaluate(FunctionExpression input, List<Expression> parameters, Expression evaluatedParameter) {
    if (!canEvaluate(input, parameters))
      return input;

    Function function = FUNCTIONS.get(input.getName());
    return function.evaluate(parameters, problemsHandler, input, evaluatedParameter);
  }

}

class IsColor extends AbstractTypeFunction {

  @Override
  protected boolean checkType(Expression parameter) {
    return parameter.getType() == ASTCssNodeType.COLOR_EXPRESSION;
  }

}

class IsKeyword extends AbstractTypeFunction {

  @Override
  protected boolean checkType(Expression parameter) {
    return parameter.getType() == ASTCssNodeType.IDENTIFIER_EXPRESSION;
  }
  
}

class IsNumber extends AbstractTypeFunction {

  @Override
  protected boolean checkType(Expression parameter) {
    return parameter.getType() == ASTCssNodeType.NUMBER;
  }
  
}

class IsString extends AbstractTypeFunction {

  @Override
  protected boolean checkType(Expression parameter) {
    return parameter.getType() == ASTCssNodeType.STRING_EXPRESSION;
  }
  
}

class IsPixel extends AbstractTypeFunction {

  @Override
  protected boolean checkType(Expression parameter) {
    return parameter.getType() == ASTCssNodeType.NUMBER && ((NumberExpression)parameter).getSuffix().equalsIgnoreCase("px");
  }
  
}

class IsPercentage extends AbstractTypeFunction {

  @Override
  protected boolean checkType(Expression parameter) {
    return parameter.getType() == ASTCssNodeType.NUMBER && ((NumberExpression)parameter).getDimension() == Dimension.PERCENTAGE;
  }
  
}

class IsEm extends AbstractTypeFunction {

  @Override
  protected boolean checkType(Expression parameter) {
    return parameter.getType() == ASTCssNodeType.NUMBER && ((NumberExpression)parameter).getSuffix().equalsIgnoreCase("em");
  }
  
}


abstract class AbstractTypeFunction extends AbstractFunction {

  @Override
  public Expression evaluate(List<Expression> parameters, ProblemsHandler problemsHandler, FunctionExpression call, Expression evaluatedParameter) {
    if (parameters.size()>1)
      problemsHandler.wrongNumberOfArgumentsToFunction(call.getParameter(), call.getName(), 1);

    Expression parameter = parameters.get(0);
    if (checkType(parameter)) {
      return new NumberExpression(parameter.getUnderlyingStructure(), 1.0, "", "true", Dimension.UNKNOWN);
    } else {
      return new NumberExpression(parameter.getUnderlyingStructure(), 0.0, "", "false", Dimension.UNKNOWN);
    }
  }

  protected abstract boolean checkType(Expression parameter);

}
