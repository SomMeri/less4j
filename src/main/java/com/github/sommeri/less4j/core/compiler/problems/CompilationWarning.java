package com.github.sommeri.less4j.core.compiler.problems;

import com.github.sommeri.less4j.LessCompiler.Problem;

public class CompilationWarning implements Problem {
  
  private final int line;
  private final int character;
  private final String message;

  public CompilationWarning(int line, int character, String message) {
    super();
    this.line = line;
    this.character = character;
    this.message = message;
  }

  @Override
  public Type getType() {
    return Type.WARNING;
  }

  @Override
  public int getLine() {
    return line;
  }

  @Override
  public int getCharacter() {
    return character;
  }

  @Override
  public String getMessage() {
    return message;
  }

}
