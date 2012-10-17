package com.github.sommeri.less4j.utils;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.core.ThreadUnsafeLessCompiler;

public class ReadmeExample {
  public static void main(String[] args) throws Less4jException {
    LessCompiler compiler = new ThreadUnsafeLessCompiler();
    String css = compiler.compile("* { margin: 1 1 1 1; }");
    System.out.println(css);
  }
}
