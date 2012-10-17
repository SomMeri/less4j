package com.github.sommeri.less4j.core;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;

public class DefaultLessCompiler implements LessCompiler {

  @Override
  public String compile(String lessContent) throws Less4jException {
    ThreadUnsafeLessCompiler compiler = new ThreadUnsafeLessCompiler();
    return compiler.compile(lessContent);
  }

}
