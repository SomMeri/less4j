package com.github.sommeri.less4j.core.compiler.expressions;

import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.EscapedValue;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;

public class TypesConversionUtils {

  public Double toDouble(CssString input) {
    try{
      return Double.parseDouble(input.getValue());
    } catch (NumberFormatException ex) {
      return Double.NaN;
    }
  }

//  public String toStringContentTypes() {
//    
//  }
  
  public String contentToString(Expression input) {
    switch (input.getType()) {
    case IDENTIFIER_EXPRESSION:
      return ((IdentifierExpression)input).getValue();

    case STRING_EXPRESSION:
      return ((CssString)input).getValue();

    case ESCAPED_VALUE:
      return ((EscapedValue)input).getValue();

    default:
      return null;
    }
  }
}
