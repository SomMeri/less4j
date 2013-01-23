package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.ColorExpression;
import com.github.sommeri.less4j.core.ast.ComposedExpression;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression.Dimension;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class MiscFunctions implements FunctionsPackage {

  protected static final String COLOR = "color";
  protected static final String UNIT = "unit";
  protected static final String CONVERT = "convert";
  protected static final String EXTRACT = "extract";

  private static Map<String, Function> FUNCTIONS = new HashMap<String, Function>();
  static {
    FUNCTIONS.put(COLOR, new Color());
    FUNCTIONS.put(UNIT, new Unit());
    FUNCTIONS.put(CONVERT, new Convert());
    FUNCTIONS.put(EXTRACT, new Extract());
  }

  private final ProblemsHandler problemsHandler;

  public MiscFunctions(ProblemsHandler problemsHandler) {
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

class Color extends AbstractMultiParameterFunction {

  @Override
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    CssString string = (CssString) splitParameters.get(0);
    return new ColorExpression(token, string.getValue());
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
    return validateParameter(parameter, ASTCssNodeType.STRING_EXPRESSION, problemsHandler);
  }

}

class Unit extends AbstractMultiParameterFunction {

  @Override
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    NumberExpression dimension = (NumberExpression) splitParameters.get(0);
    IdentifierExpression unit = splitParameters.size() > 1 ? (IdentifierExpression) splitParameters.get(1) : null;

    String newSuffix;
    Dimension newDimension;
    if (unit != null) {
      newSuffix = unit.getValue();
      newDimension = Dimension.forSuffix(newSuffix);
    } else {
      newSuffix = "";
      newDimension = Dimension.NUMBER;
    }

    return new NumberExpression(token, dimension.getValueAsDouble(), newSuffix, null, newDimension);
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
    switch (position) {
    case 0:
      return validateParameter(parameter, ASTCssNodeType.NUMBER, problemsHandler);
    case 1:
      return validateParameter(parameter, ASTCssNodeType.IDENTIFIER_EXPRESSION, problemsHandler);
    }
    return false;
  }

}

class Convert extends AbstractMultiParameterFunction {

  @Override
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    NumberExpression value = (NumberExpression) splitParameters.get(0);
    IdentifierExpression unit = (IdentifierExpression) splitParameters.get(1);
    return value.convertTo(unit.getValue());
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
    switch (position) {
    case 0:
      return validateParameter(parameter, ASTCssNodeType.NUMBER, problemsHandler);
    case 1:
      return validateParameter(parameter, ASTCssNodeType.IDENTIFIER_EXPRESSION, problemsHandler);
    }
    return false;
  }

}

class Extract extends AbstractMultiParameterFunction {

  @Override
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, HiddenTokenAwareTree token) {
    List<Expression> values = collect((ComposedExpression) splitParameters.get(0));
    NumberExpression index = (NumberExpression) splitParameters.get(1);
    return values.get(index.getValueAsDouble().intValue() - 1);
  }
  
  private List<Expression> collect(ComposedExpression values) {
    List<Expression> result = new ArrayList<Expression>();
    Expression left = values.getLeft();
    Expression right = values.getRight();
    
    if (left.getType() == ASTCssNodeType.COMPOSED_EXPRESSION) {
      result.addAll(collect((ComposedExpression) left));
    } else {
      result.add(left);
    }
    if (right.getType() == ASTCssNodeType.COMPOSED_EXPRESSION) {
      result.addAll(collect((ComposedExpression) right));
    } else {
      result.add(right);
    }
    return result;
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
    switch (position) {
    case 0:
      return validateParameter(parameter, ASTCssNodeType.COMPOSED_EXPRESSION, problemsHandler);
    case 1:
      return validateParameter(parameter, ASTCssNodeType.NUMBER, problemsHandler);
    }
    return false;
  }
  
}
