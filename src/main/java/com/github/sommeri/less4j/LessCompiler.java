package com.github.sommeri.less4j;

import java.io.File;
import java.util.Collections;
import java.util.List;

public interface LessCompiler {

  public CompilationResult compile(String lessContent) throws Less4jException;

  public CompilationResult compile(File inputFile) throws Less4jException;

  public class CompilationResult {

    private final String css;
    private final List<Problem> warnings;

    public CompilationResult(String css) {
      this(css, emptyList());
    }

    private static List<Problem> emptyList() {
      return Collections.emptyList();
    }

    public CompilationResult(String css, List<Problem> warnings) {
      super();
      this.css = css;
      this.warnings = warnings;
    }

    public String getCss() {
      return css;
    }

    public List<Problem> getWarnings() {
      return warnings;
    }

  }

  public interface Problem {

    public Type getType();

    public File getFile();

    public int getLine();

    public int getCharacter();

    public String getMessage();

    public enum Type {
      WARNING, ERROR
    }

  }

}
