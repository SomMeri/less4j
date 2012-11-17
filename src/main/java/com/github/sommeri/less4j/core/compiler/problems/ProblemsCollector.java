package com.github.sommeri.less4j.core.compiler.problems;

import java.util.ArrayList;
import java.util.Collection;

import com.github.sommeri.less4j.LessCompiler.Problem;

public class ProblemsCollector {
  
  private Collection<Problem> warnings = new ArrayList<Problem>();
  private Collection<Problem> errors = new ArrayList<Problem>();
  
  public void addError(CompilationError error) {
    errors.add(error);
  }
  
  

}
