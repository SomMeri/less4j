package com.github.sommeri.less4j.core.problems;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.AbstractProblem;
import com.github.sommeri.less4j.core.ast.ASTCssNode;

public class CompilationError extends AbstractProblem {
  
  private final LessSource source;
  private final int line;
  private final int character;
  private final String message;

  public CompilationError(LessSource source, int line, int character, String message) {
    super();
    this.source = source;
    this.line = line;
    this.character = character;
    this.message = message;
  }

  public CompilationError(ASTCssNode offendingNode, String message) {
    super();
    this.source = offendingNode.getSource();
    this.line = offendingNode.getSourceLine();
    this.character = offendingNode.getCharPositionInSourceLine();
    this.message = message;
  }

  @Override
  public Type getType() {
    return Type.ERROR;
  }

  @Override
  public LessSource getSource() {
	return source;
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
