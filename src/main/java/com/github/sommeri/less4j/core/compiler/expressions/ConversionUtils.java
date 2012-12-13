package com.github.sommeri.less4j.core.compiler.expressions;

import com.github.sommeri.less4j.core.ast.CssString;

public class ConversionUtils {

  //FIXME: a tool that finds all kinds of runtime exceptions a method can throw 
  public Double toDouble(CssString input) {
    try{
      return Double.parseDouble(input.getValue());
    } catch (NumberFormatException ex) {
      return Double.NaN;
    }
     
  }
}
