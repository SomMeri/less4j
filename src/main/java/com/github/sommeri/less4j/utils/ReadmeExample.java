package com.github.sommeri.less4j.utils;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessCompiler.Problem;
import com.github.sommeri.less4j.core.ThreadUnsafeLessCompiler;

//FIXME: !!!! change readme
public class ReadmeExample {
  public static void main(String[] args) throws Less4jException {
    LessCompiler compiler = new ThreadUnsafeLessCompiler();
    CompilationResult compilationResult = compiler.compile("* { margin: 1 1 1 1; }");

    System.out.println(compilationResult.getCss());
    for (Problem warning : compilationResult.getWarnings()) {
      System.err.println(format(warning));
    }
    
  }

  private static String format(Problem warning) {
    return "WARNING " + warning.getLine() +":" + warning.getCharacter()+ " " + warning.getMessage();
  }
}
