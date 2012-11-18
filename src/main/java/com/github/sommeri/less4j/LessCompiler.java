package com.github.sommeri.less4j;

import java.util.List;

public interface LessCompiler {

  public CompilationResult compile(String lessContent) throws Less4jException;

  public class CompilationResult {

    private final String css;
    private final List<Problem> warnings;

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

    public int getLine();

    public int getCharacter();

    public String getMessage();

    public enum Type {
      WARNING, ERROR
    }
  }

}
