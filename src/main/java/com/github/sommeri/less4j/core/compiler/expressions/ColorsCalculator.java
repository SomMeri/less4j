package com.github.sommeri.less4j.core.compiler.expressions;

import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.ColorExpression;
import com.github.sommeri.less4j.core.ast.ComposedExpression;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.ExpressionOperator;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.compiler.CompileException;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

//TODO document: color code shortcut
class ColorsCalculator {

  private static final int MIN = 0;
  private static final int MAX = 255;

  public ColorExpression evalute(ComposedExpression originalExpression, Expression first, Expression second) {
    double red1 = calcRed(first);
    double green1 = calcGreen(first);
    double blue1 = calcBlue(first);

    double red2 = calcRed(second);
    double green2 = calcGreen(second);
    double blue2 = calcBlue(second);

    ExpressionOperator operator = originalExpression.getOperator();
    switch (operator.getOperator()) {
    case SOLIDUS:
      return divide(first, red1, green1, blue1, red2, green2, blue2, originalExpression.getUnderlyingStructure());
    case STAR:
      return multiply(red1, green1, blue1, red2, green2, blue2, originalExpression.getUnderlyingStructure());
    case MINUS:
      return subtract(first, red1, green1, blue1, red2, green2, blue2, originalExpression.getUnderlyingStructure());
    case PLUS:
      return add(red1, green1, blue1, red2, green2, blue2, originalExpression.getUnderlyingStructure());

    case COMMA:
    case EMPTY_OPERATOR:
      throw new CompileException("Not a color operator.", operator);

    default:
      throw new CompileException("Unknown operator.", operator);
    }

  }

  private ColorExpression subtract(Expression first, double red1, double green1, double blue1, double red2, double green2, double blue2, HiddenTokenAwareTree parentToken) {
    if (first.getType()==ASTCssNodeType.NUMBER)
      throw new CompileException("Can't substract or divide a color from a number", first);
    
    return createResultColor(parentToken, round(red1 - red2), round(green1 - green2), round(blue1 - blue2));
  }

  private ColorExpression multiply(double red1, double green1, double blue1, double red2, double green2, double blue2, HiddenTokenAwareTree parentToken) {
    return createResultColor(parentToken, round(red1 * red2), round(green1 * green2), round(blue1 * blue2));
  }

  private ColorExpression divide(Expression first, double red1, double green1, double blue1, double red2, double green2, double blue2, HiddenTokenAwareTree parentToken) {
    if (first.getType()==ASTCssNodeType.NUMBER)
      throw new CompileException("Can't substract or divide a color from a number", first);
    
    return createResultColor(parentToken, round(red1 / red2), round(green1 / green2), round(blue1 / blue2));
  }

  private ColorExpression add(double red1, double green1, double blue1, double red2, double green2, double blue2, HiddenTokenAwareTree parentToken) {
    return createResultColor(parentToken, round(red1 + red2), round(green1 + green2), round(blue1 + blue2));
  }

  private int round(double number) {
    if (number > MAX)
      return MAX;
    
    return number<MIN? MIN : (int)Math.round(number);
  }

  private ColorExpression createResultColor(HiddenTokenAwareTree parentToken, double red, double green, double blue) {
    return new ColorExpression(parentToken, round(red), round(green), round(blue));
  }

  public boolean accepts(ExpressionOperator operator, Expression first, Expression second) {
    if (!acceptedOperand(first, second))
      return false;
    
    return acceptedOperator(operator);
  }

  private boolean acceptedOperator(ExpressionOperator operator) {
    return operator.getOperator() != ExpressionOperator.Operator.COMMA && operator.getOperator() != ExpressionOperator.Operator.EMPTY_OPERATOR;
  }

  private boolean acceptedOperand(Expression first, Expression second) {
    //at least one color and both have to be either color or a number
    if (first.getType() != ASTCssNodeType.COLOR_EXPRESSION && first.getType() != ASTCssNodeType.NUMBER)
      return false;

    if (second.getType() != ASTCssNodeType.COLOR_EXPRESSION && second.getType() != ASTCssNodeType.NUMBER)
      return false;
    
    return first.getType() == ASTCssNodeType.COLOR_EXPRESSION || second.getType() == ASTCssNodeType.COLOR_EXPRESSION;
  }

  private double calcRed(Expression value) {
    if (value instanceof ColorExpression) {
      return ((ColorExpression) value).getRed();
    }
    
    return ((NumberExpression) value).getValueAsDouble();
  }

  private double calcGreen(Expression value) {
    if (value instanceof ColorExpression) {
      return ((ColorExpression) value).getGreen();
    }
    
    return ((NumberExpression) value).getValueAsDouble();
  }

  private double calcBlue(Expression value) {
    if (value instanceof ColorExpression) {
      return ((ColorExpression) value).getBlue();
    }
    
    return ((NumberExpression) value).getValueAsDouble();
  }

}
