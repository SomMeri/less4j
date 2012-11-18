package com.github.sommeri.less4j;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessCompiler.Problem;
import com.github.sommeri.less4j.core.TranslationException;

@SuppressWarnings("serial")
public class Less4jException extends Exception {

  private final CompilationResult partialResult;
  private final List<Problem> errors;

  public Less4jException(List<Problem> errors, CompilationResult partialResult) {
    super();
    this.errors = errors;
    this.partialResult = partialResult;
  }

  public Less4jException(TranslationException ex) {
    super(ex);
    this.errors = Collections.emptyList();
    this.partialResult = null;
  }

  @Override
  public synchronized TranslationException getCause() {
    return (TranslationException) super.getCause();
  }

  public CompilationResult getPartialResult() {
    return partialResult;
  }

  public List<Problem> getErrors() {
    return errors;
  }

}
