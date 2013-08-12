package com.github.sommeri.less4j;

import java.util.Arrays;
import java.util.List;

import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessCompiler.Problem;
import com.github.sommeri.less4j.utils.ArraysUtils;
import com.github.sommeri.less4j.utils.ProblemsPrinter;

@SuppressWarnings("serial")
public class Less4jException extends Exception {

  private static final int SHOW_ERRORS = 2;
  private final CompilationResult partialResult;
  private final List<Problem> errors;
  private final String message;

  public Less4jException(Problem error, CompilationResult partialResult) {
   this(Arrays.asList(error), partialResult); 
  }
  
  public Less4jException(List<Problem> errors, CompilationResult partialResult) {
    super();
    this.errors = errors;
    this.partialResult = partialResult;
    this.message = createMessage();
  }

  public CompilationResult getPartialResult() {
    return partialResult;
  }

  public List<Problem> getErrors() {
    return errors;
  }
  
  @Override
  public String getMessage() {
    return message;
  }

  private String createMessage() {
    ProblemsPrinter problemsPrinter = new ProblemsPrinter();
    
    StringBuilder builder = new StringBuilder("Could not compile less. ");
    builder.append(errors.size()).append(" error(s) occured:\n");
    
    List<Problem> visibleErrors = ArraysUtils.safeSublist(errors, 0, SHOW_ERRORS);
    String visibleErrorsStr = problemsPrinter.printErrors(visibleErrors);
    builder.append(visibleErrorsStr);
    
    if (errors.size()>SHOW_ERRORS)
      builder.append("...\n");
    
    return  builder.toString();
  }
}
