package com.github.sommeri.less4j.core.compiler;

import com.github.sommeri.less4j.core.ast.ColorExpression;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.SignedExpression;
import com.github.sommeri.less4j.core.ast.SignedExpression.Sign;

//FIXME: pattern must be pattern, not evaluated number of something <- only in top level
public class PatternsComparator {

  public boolean equal(Expression pattern, Expression expression) {
    switch (pattern.getType()) {
    case FUNCTION:
    case COMPOSED_EXPRESSION:
    case INDIRECT_VARIABLE:
    case VARIABLE:
    case PARENTHESES_EXPRESSION:
    case NAMED_EXPRESSION:
      return false;

      //TODO: rename to signed_expression
    case NEGATED_EXPRESSION:
      return equalNegated((SignedExpression) pattern, expression);

    case IDENTIFIER_EXPRESSION:
      return equalIdentifier((IdentifierExpression) pattern, expression);

    case NUMBER:
      return equalNumber((NumberExpression) pattern, expression);

    case STRING_EXPRESSION:
      return equalString((CssString) pattern, expression);

    case COLOR_EXPRESSION:
      return equalColor((ColorExpression) pattern, expression);

    default:
      throw new CompileException("Unknown expression type", pattern);
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
      //FIXME: document that matching depends also on quote type
      return equals(pattern.getValue(), string.getValue()) && equals(pattern.getQuoteType(), string.getQuoteType());
    }

    return false;
  }

  private boolean equalNumber(NumberExpression pattern, Expression expression) {
    if (expression instanceof NumberExpression) {
      NumberExpression numberExpression = (NumberExpression) expression;
      return equals(pattern.getSuffix(), numberExpression.getSuffix()) && equals(pattern.getValueAsDouble(), numberExpression.getValueAsDouble());
    }

    return false;
  }

  private boolean equalIdentifier(IdentifierExpression pattern, Expression expression) {
    if (expression instanceof IdentifierExpression) {
      return equals(pattern.getValue(), ((IdentifierExpression) expression).getValue());
    }

    return false;
  }

  private boolean equalNegated(SignedExpression pattern, Expression expression) {
    if (expression instanceof SignedExpression) {
      return equalNegated(pattern, (SignedExpression) expression);
    }
    if (expression instanceof NumberExpression && pattern.getExpression() instanceof NumberExpression) {
      NumberExpression patternClone = (NumberExpression)pattern.getExpression().clone();
      
      if (pattern.getSign()==Sign.MINUS)
        patternClone.setValueAsDouble(patternClone.getValueAsDouble()*-1);
      //TODO test on negative pixel and other edge cases
      NumberExpression number = (NumberExpression) expression;
      return equalNumber(patternClone, number);
    }

    return false;
  }

  private boolean equalNegated(SignedExpression pattern, SignedExpression expression) {
    return equal(pattern.getExpression(), expression.getExpression());
  }

  private boolean equals(Object value, Object value2) {
    if (value == null)
      return value2 == null;
    
    return value.equals(value2);
  }

  private boolean equals(Double value, Double value2) {
    if (value == null)
      return value2 == null;
    
    value = normalize0(value);
    value2 = normalize0(value2);
    return value.compareTo(value2)==0;
  }

  private Double normalize0(Double value) {
    if (value.compareTo(-0.0)==0)
      value = 0.0;
    return value;
  }
}
