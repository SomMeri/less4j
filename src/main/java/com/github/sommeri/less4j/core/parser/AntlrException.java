package com.github.sommeri.less4j.core.parser;

import java.io.File;
import java.net.URL;

import org.antlr.runtime.RecognitionException;

import com.github.sommeri.less4j.LessCompiler.Problem;

public class AntlrException implements Problem {
  
  private final RecognitionException delegee;
  private final URL file;
  private String message;

  public AntlrException(URL file, RecognitionException delegee, String message) {
    super();
    this.delegee = delegee;
    this.file=file;
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
  public URL getFile() {
    return file;
  }
  
}
