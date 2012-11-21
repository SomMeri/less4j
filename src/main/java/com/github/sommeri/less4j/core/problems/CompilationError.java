package com.github.sommeri.less4j.core.problems;

import com.github.sommeri.less4j.LessCompiler.Problem;
import com.github.sommeri.less4j.core.ast.ASTCssNode;

public class CompilationError implements Problem {
  
  private final int line;
  private final int character;
  private final String message;

  public CompilationError(int line, int character, String message) {
    super();
    this.line = line;
    this.character = character;
    this.message = message;
  }

  public CompilationError(ASTCssNode offendingNode, String message) {
    super();
    this.line = offendingNode.getSourceLine();
    this.character = offendingNode.getCharPositionInSourceLine();
    this.message = message;
  }

  @Override
  public Type getType() {
    return Type.ERROR;
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
