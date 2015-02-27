package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression.Dimension;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class TypeFunctions extends BuiltInFunctionsPack {

  protected static final String ISCOLOR = "iscolor";
  protected static final String IS_RULESET = "isruleset";
  protected static final String ISKEYWORD = "iskeyword";
  protected static final String ISNUMBER = "isnumber";
  protected static final String ISSTRING = "isstring";
  protected static final String ISPIXEL = "ispixel";
  protected static final String ISPERCENTAGE = "ispercentage";
  protected static final String ISEM = "isem";
  protected static final String ISURL = "isurl";
  protected static final String ISUNIT = "isunit";

  private static Map<String, Function> FUNCTIONS = new HashMap<String, Function>();
  static {
    FUNCTIONS.put(ISCOLOR, new IsColor());
    FUNCTIONS.put(IS_RULESET, new IsRuleset());
    FUNCTIONS.put(ISKEYWORD, new IsKeyword());
    FUNCTIONS.put(ISNUMBER, new IsNumber());
    FUNCTIONS.put(ISSTRING, new IsString());
    FUNCTIONS.put(ISPIXEL, new IsPixel());
    FUNCTIONS.put(ISPERCENTAGE, new IsPercentage());
    FUNCTIONS.put(ISEM, new IsEm());
    FUNCTIONS.put(ISURL, new IsUrl());
    FUNCTIONS.put(ISUNIT, new IsUnit());
  }

  public TypeFunctions(ProblemsHandler problemsHandler) {
    super(problemsHandler);
  }

  @Override
  protected Map<String, Function> getFunctions() {
    return FUNCTIONS;
  }

}

class IsColor extends AbstractTypeFunction {

  @Override
  protected boolean checkType(Expression parameter) {
    return parameter.getType() == ASTCssNodeType.COLOR_EXPRESSION;
  }

}

class IsRuleset extends AbstractTypeFunction {

  @Override
  protected boolean checkType(Expression parameter) {
    return parameter.getType() == ASTCssNodeType.DETACHED_RULESET;
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

class IsUrl extends AbstractTypeFunction {

  @Override
  protected boolean checkType(Expression parameter) {
    if (parameter.getType() != ASTCssNodeType.FUNCTION)
      return false;
    
    FunctionExpression function = (FunctionExpression) parameter;
    return function.getName().equalsIgnoreCase("url");
  }
  
}

class IsPixel extends AbstractTypeFunction {

  @Override
  protected boolean checkType(Expression parameter) {
    return parameter.getType() == ASTCssNodeType.NUMBER && ((NumberExpression)parameter).getSuffix().equalsIgnoreCase("px");
  }
  
}

class IsUnit extends CatchAllMultiParameterFunction {
  
  private final TypesConversionUtils utils = new TypesConversionUtils();

  @Override
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, FunctionExpression functionCall, HiddenTokenAwareTree token) {
    Expression expression = splitParameters.get(0);
    Expression unitExpression = splitParameters.get(1);
    
    String unit = utils.contentToString(unitExpression);
    if (expression.getType()!=ASTCssNodeType.NUMBER || unit==null)
      return new IdentifierExpression(functionCall.getUnderlyingStructure(), "false");
    
    return isunit((NumberExpression) expression, unit, functionCall.getUnderlyingStructure());
  }

  private Expression isunit(NumberExpression expression, String unit, HiddenTokenAwareTree token) {
    String suffix = expression.getSuffix();
    String result="true"; 
    if (suffix == null || unit.isEmpty() || !suffix.equals(unit))
      result="false";
    
    return new IdentifierExpression(token, result);
  }

  @Override
  protected boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler) {
    return true;
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
  protected String getName() {
    return TypeFunctions.ISUNIT;
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
      problemsHandler.wrongNumberOfArgumentsToFunctionMin(call.getParameter(), call.getName(), 1);

    Expression parameter = parameters.get(0);
    if (checkType(parameter)) {
      return new IdentifierExpression(parameter.getUnderlyingStructure(), "true");
    } else {
      return new IdentifierExpression(parameter.getUnderlyingStructure(), "false");
    }
  }

  protected abstract boolean checkType(Expression parameter);

}
