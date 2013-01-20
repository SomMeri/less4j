package com.github.sommeri.less4j.core.problems;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.AbstractProblem;

public class GeneralProblem extends AbstractProblem {

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
  public LessSource getSource() {
	return null;
  }

}
