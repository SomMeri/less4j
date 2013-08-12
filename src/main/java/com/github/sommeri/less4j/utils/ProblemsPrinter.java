package com.github.sommeri.less4j.utils;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.sommeri.less4j.LessCompiler.Problem;
import com.github.sommeri.less4j.LessSource;

public class ProblemsPrinter {
  
  private SourceNamePrinter sourceNamePrinter = new AbsoluteSourceNamePrinter();
  
  public ProblemsPrinter() {
  }

  public ProblemsPrinter(SourceNamePrinter sourceNamePrinter) {
    this.sourceNamePrinter = sourceNamePrinter;
  }

  public String printWarnings(List<Problem >warnings) {
    StringBuilder builder = new StringBuilder();
    for (Problem warning : warnings) {
      builder.append(toWarning(warning, sourceNamePrinter)).append("\n");
    }
    return builder.toString();
  }

  public String printErrors(List<Problem> errors) {
    StringBuilder builder = new StringBuilder();
    Set<String> previousMessages = new HashSet<String>();
    
    for (Problem error : errors) {
      String message = toError(error, sourceNamePrinter);
      if (!previousMessages.contains(message)) {
        builder.append(message).append("\n");
        previousMessages.add(message);
      }
    }
    return builder.toString();
  }

  private String toWarning(Problem warning, SourceNamePrinter sourceNamePrinter) {
    return "WARNING " + toString(warning, sourceNamePrinter);
  }

  private String toError(Problem warning, SourceNamePrinter sourceNamePrinter) {
    return "ERROR " + toString(warning, sourceNamePrinter);
  }

  private String toString(Problem problem, SourceNamePrinter sourceNamePrinter) {
    String filename = sourceNamePrinter.printSourceName(problem.getSource());
    if (!filename.isEmpty())
      filename = filename + " ";
    String lineChar = toLineCharReport(problem);

    if (!lineChar.isEmpty())
      lineChar = lineChar + " ";

    return filename + lineChar + problem.getMessage();
  }

  private String toLineCharReport(Problem problem) {
    if (problem.getLine() == -1 || problem.getCharacter() == -1)
      return "";

    return problem.getLine() + ":" + problem.getCharacter();
  }

  public interface SourceNamePrinter {
    String printSourceName(LessSource source);
  }
  
  public static class AbsoluteSourceNamePrinter implements SourceNamePrinter {

    @Override
    public String printSourceName(LessSource source) {
      if (source==null)
        return "";
      
      URI uri = source.getURI();
      return uri==null ? "" : uri.toString();
    }
    
  }

}
