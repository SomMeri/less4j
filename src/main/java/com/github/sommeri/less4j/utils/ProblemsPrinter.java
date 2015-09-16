package com.github.sommeri.less4j.utils;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.sommeri.less4j.LessCompiler.Problem;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.LessSource.CannotReadFile;
import com.github.sommeri.less4j.LessSource.FileNotFound;

public class ProblemsPrinter {

  private SourceNamePrinter sourceNamePrinter = new AbsoluteSourceNamePrinter();

  public ProblemsPrinter() {
  }

  public ProblemsPrinter(SourceNamePrinter sourceNamePrinter) {
    this.sourceNamePrinter = sourceNamePrinter;
  }

  public String printWarnings(List<Problem> warnings) {
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

    String snippet = codeSnippet(problem);

    return filename + lineChar + problem.getMessage() + snippet;
  }

  private String codeSnippet(Problem problem) {
    StringBuilder result = new StringBuilder();
    
    String[] lines = getContentLines(problem);
    if (lines == null || lines.length == 0)
      return result.toString();

    int errorLine = problem.getLine();
    int start = errorLine - 1;
    int end = errorLine + 1;

    start = start < 1 ? 1 : start;
    end = end > lines.length ? lines.length : end;
    
    int numberLength  = String.valueOf(end).length()+1;
    for (int i = start; i<=end; i++) {
      result.append("\n").append(toPaddedNumber(i, numberLength)).append(": ");
      result.append(lines[i-1]);
    }
    result.append("\n");
    return result.toString();
  }

  private String toPaddedNumber(int number, int length) {
    String numStr = String.valueOf(number);
    
    char[] prefix = new char[length - numStr.length()];
    Arrays.fill(prefix, ' ');
    return new String(prefix)  + numStr;
  }

  private String[] getContentLines(Problem problem) {
    try {
      LessSource source = problem.getSource();
      if (source==null)
        return new String[0];
      
      String content = source.getContent();
      if (content == null)
        return new String[0];

      return content.split("\n");
    } catch (FileNotFound e) {
    } catch (CannotReadFile e) {
    }

    return new String[0];
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
      if (source == null)
        return "";

      URI uri = source.getURI();
      if (uri!=null)
        return uri.toString();
      
      String name = source.getName();
      return name == null ? "" : name;
    }

  }

}
