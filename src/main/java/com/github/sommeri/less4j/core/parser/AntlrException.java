package com.github.sommeri.less4j.core.parser;

import org.antlr.runtime.RecognitionException;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.AbstractProblem;

public class AntlrException extends AbstractProblem {
  
  private final RecognitionException delegee;
  private final LessSource source;
  private String message;

  public AntlrException(LessSource source, RecognitionException delegee, String message) {
    super();
    this.delegee = delegee;
    this.source=source;
    this.message = message==null? null : message.trim();
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public int getLine() {
    return delegee.line;
  }

  public int getCharacter() {
    return delegee.charPositionInLine+1;
  }

  @Override
  public Type getType() {
    return Type.ERROR;
  }

  @Override
  public LessSource getSource() {
    return source;
  }
  
}
