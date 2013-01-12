package com.github.sommeri.less4j;

import java.util.Arrays;
import java.util.List;

import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessCompiler.Problem;

@SuppressWarnings("serial")
public class Less4jException extends Exception {

  private final CompilationResult partialResult;
  private final List<Problem> errors;

  public Less4jException(Problem error, CompilationResult partialResult) {
   this(Arrays.asList(error), partialResult); 
  }
  
  public Less4jException(List<Problem> errors, CompilationResult partialResult) {
    super();
    this.errors = errors;
    this.partialResult = partialResult;
  }

  public CompilationResult getPartialResult() {
    return partialResult;
  }

  public List<Problem> getErrors() {
    return errors;
  }
  
}
