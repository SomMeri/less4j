package com.github.sommeri.less4j.core;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.LessCompiler;

public class DummyLessCompiler implements LessCompiler {

  public String compile(String cssContent) {
    return cssContent;
  }

  @Override
  public List<IProblem> getProblems() {
    return new ArrayList<IProblem>();
  }

}
