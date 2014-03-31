package com.github.sommeri.less4j.core.compiler.expressions;

import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.ColorExpression;
import com.github.sommeri.less4j.core.ast.ColorExpression.ColorWithAlphaExpression;
import com.github.sommeri.less4j.core.ast.BinaryExpression;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.BinaryExpressionOperator;
import com.github.sommeri.less4j.core.ast.FaultyExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.problems.BugHappened;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

class ColorsCalculator {

  private final ProblemsHandler problemsHandler;
  private static final int MIN = 0;
  private static final int MAX = 255;
  private static final double ALPHA_MIN = 0;
  private static final double ALPHA_MAX = 1.0;
  private static final double ALPHA_EPSILON = 0.0000000000000001;
  
  public ColorsCalculator(ProblemsHandler problemsHandler) {
    super();
    this.problemsHandler = problemsHandler;
  }

  public Expression evalute(BinaryExpression originalExpression, Expression first, Expression second) {
    double red1 = calcRed(first);
    double green1 = calcGreen(first);
    double blue1 = calcBlue(first);
    double alpha1 = calcAlpha(first);

    double red2 = calcRed(second);
    double green2 = calcGreen(second);
    double blue2 = calcBlue(second);
    double alpha2 = calcAlpha(second);

    BinaryExpressionOperator operator = originalExpression.getOperator();
    switch (operator.getOperator()) {
    case SOLIDUS:
      return divide(first, red1, green1, blue1, alpha1,  red2, green2, blue2, alpha2, originalExpression.getUnderlyingStructure());
    case STAR:
      return multiply(red1, green1, blue1, alpha1, red2, green2, blue2, alpha2, originalExpression.getUnderlyingStructure());
    case MINUS:
      return subtract(first, red1, green1, blue1, alpha1, red2, green2, blue2, alpha2, originalExpression.getUnderlyingStructure());
    case PLUS:
      return add(red1, green1, blue1, alpha1, red2, green2, blue2, alpha2, originalExpression.getUnderlyingStructure());

    default:
      throw new BugHappened("Unknown operator.", operator);
    }

  }

  private Expression subtract(Expression first, double red1, double green1, double blue1, double alpha1, double red2, double green2, double blue2, double alpha2, HiddenTokenAwareTree parentToken) {
    if (first.getType()==ASTCssNodeType.NUMBER) {
      problemsHandler.subtractOrDiveColorFromNumber(first);
      return new FaultyExpression(first);
    }
    
    return createResultColor(parentToken, round(red1 - red2), round(green1 - green2), round(blue1 - blue2), alpha1, alpha2);
  }

  private ColorExpression multiply(double red1, double green1, double blue1, double alpha1, double red2, double green2, double blue2, double alpha2, HiddenTokenAwareTree parentToken) {
    return createResultColor(parentToken, round(red1 * red2), round(green1 * green2), round(blue1 * blue2), alpha1, alpha2);
  }

  private Expression divide(Expression first, double red1, double green1, double blue1, double alpha1, double red2, double green2, double blue2, double alpha2, HiddenTokenAwareTree parentToken) {
    if (first.getType()==ASTCssNodeType.NUMBER) {
      problemsHandler.subtractOrDiveColorFromNumber(first);
      return new FaultyExpression(first);
    }
    
    return createResultColor(parentToken, round(red1 / red2), round(green1 / green2), round(blue1 / blue2), alpha1, alpha2);
  }

  private ColorExpression add(double red1, double green1, double blue1, double alpha1, double red2, double green2, double blue2, double alpha2, HiddenTokenAwareTree parentToken) {
    return createResultColor(parentToken, round(red1 + red2), round(green1 + green2), round(blue1 + blue2), alpha1, alpha2);
  }

  private int round(double number) {
    if (number > MAX)
      return MAX;
    
    return number<MIN? MIN : (int)Math.round(number);
  }

  private double roundAlpha(double alpha) {
    if (alpha > ALPHA_MAX)
      return ALPHA_MAX;
    
    return alpha<ALPHA_MIN? ALPHA_MIN : alpha;
  }

  private ColorExpression createResultColor(HiddenTokenAwareTree parentToken, double red, double green, double blue, double alpha1, double alpha2) {
    double roundAlpha = roundAlpha(alpha1+alpha2);
    if (roundAlpha<ALPHA_MAX-ALPHA_EPSILON)
      return new ColorWithAlphaExpression(parentToken, round(red), round(green), round(blue), roundAlpha);
    
    return new ColorExpression(parentToken, round(red), round(green), round(blue));
  }

  public boolean accepts(BinaryExpressionOperator operator, Expression first, Expression second) {
    return acceptedOperand(first, second);
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

  private double calcAlpha(Expression value) {
    if (value instanceof ColorWithAlphaExpression) {
      return ((ColorWithAlphaExpression) value).getAlpha();
    }

    if (value instanceof ColorExpression) {
      return 1.0;
    }
    
    return Double.POSITIVE_INFINITY;
  }
}
