package com.github.sommeri.less4j.core.compiler.expressions;

import com.github.sommeri.less4j.core.ast.ColorExpression;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.SignedExpression;
import com.github.sommeri.less4j.core.ast.SignedExpression.Sign;
import com.github.sommeri.less4j.core.compiler.CompileException;

public abstract class ExpressionComparator {

  public boolean isSimpleExpression(Expression expression) {
    switch (expression.getType()) {
    case SIGNED_EXPRESSION:
    case IDENTIFIER_EXPRESSION:
    case NUMBER:
    case STRING_EXPRESSION:
    case COLOR_EXPRESSION:
      return true;
    }
    return false;
  }

  public void validateSimpleExpression(Expression expression) {
    if (!isSimpleExpression(expression))
      throw new CompileException("The expression must evaluate to simple expression (e.g. identifier, number, string or color).", expression);
  }

  public boolean equal(Expression pattern, Expression expression) {
    switch (pattern.getType()) {
    case SIGNED_EXPRESSION:
      return equalSigned((SignedExpression) pattern, expression);

    case IDENTIFIER_EXPRESSION:
      return equalIdentifier((IdentifierExpression) pattern, expression);

    case NUMBER:
      return equalNumber((NumberExpression) pattern, expression);

    case STRING_EXPRESSION:
      return equalString((CssString) pattern, expression);

    case COLOR_EXPRESSION:
      return equalColor((ColorExpression) pattern, expression);

    default:
      return false;
    }
  }

  private boolean equalColor(ColorExpression pattern, Expression expression) {
    if (expression instanceof ColorExpression) {
      return equals(pattern.getValue(), ((ColorExpression) expression).getValue());
    }

    return false;
  }

  private boolean equalString(CssString pattern, Expression expression) {
    if (expression instanceof CssString) {
      CssString string = (CssString) expression;
      return equals(pattern.getValue(), string.getValue()) && equals(pattern.getQuoteType(), string.getQuoteType());
    }

    return false;
  }

  private boolean equalNumber(NumberExpression pattern, Expression expression) {
    if (expression instanceof NumberExpression) {
      NumberExpression numberExpression = (NumberExpression) expression;
      return numberEqual(pattern, numberExpression);
    }

    return false;
  }

  protected abstract boolean numberEqual(NumberExpression pattern, NumberExpression numberExpression);

  private boolean equalIdentifier(IdentifierExpression pattern, Expression expression) {
    if (expression instanceof IdentifierExpression) {
      return equals(pattern.getValue(), ((IdentifierExpression) expression).getValue());
    }

    return false;
  }

  private boolean equalSigned(SignedExpression pattern, Expression expression) {
    if (expression instanceof SignedExpression) {
      return equalNegated(pattern, (SignedExpression) expression);
    }
    if (expression instanceof NumberExpression && pattern.getExpression() instanceof NumberExpression) {
      NumberExpression patternClone = (NumberExpression) pattern.getExpression().clone();

      if (pattern.getSign() == Sign.MINUS)
        patternClone.setValueAsDouble(patternClone.getValueAsDouble() * -1);

      NumberExpression number = (NumberExpression) expression;
      return equalNumber(patternClone, number);
    }

    return false;
  }

  private boolean equalNegated(SignedExpression pattern, SignedExpression expression) {
    return equal(pattern.getExpression(), expression.getExpression());
  }

  protected boolean equals(Object value, Object value2) {
    if (value == null)
      return value2 == null;

    return value.equals(value2);
  }

  protected boolean equals(Double value, Double value2) {
    if (value == null)
      return value2 == null;

    value = normalize0(value);
    value2 = normalize0(value2);
    return value.compareTo(value2) == 0;
  }

  private Double normalize0(Double value) {
    if (value.compareTo(-0.0) == 0)
      value = 0.0;
    return value;
  }
  
}
