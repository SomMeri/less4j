package com.github.sommeri.less4j.core.problems;

import java.io.File;

import com.github.sommeri.less4j.LessCompiler.Problem;

public class GeneralProblem implements Problem {

  private final String message;

  public GeneralProblem(String message) {
    super();
    this.message = message;
  }

  @Override
  public Type getType() {
    return Type.ERROR;
  }

  @Override
  public int getLine() {
    return -1;
  }

  @Override
  public int getCharacter() {
    return -1;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public File getFile() {
    return null;
  }

}
