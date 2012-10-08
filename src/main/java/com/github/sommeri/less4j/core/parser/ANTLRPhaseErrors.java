package com.github.sommeri.less4j.core.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.Less4jException;

@SuppressWarnings("serial")
public class ANTLRPhaseErrors extends Less4jException {

  private final List<AntlrException> errors;
  private String message;

  public ANTLRPhaseErrors(List<AntlrException> errors) {
    super("ANTLR detected one or more problems: ");
    this.errors = Collections.unmodifiableList(errors);
    this.message = "ANTLR detected one or more problems: " + getPositionInformation();
  }

  @Override
  public String getMessage() {
    return message;
  }


  @Override
  public boolean hasErrorPosition() {
    return errors!=null && !errors.isEmpty();
  }

  @Override
  public int getCharPositionInLine() {
    return -1;
  }

  @Override
  public int getLine() {
    return -1;
  }

  @Override
  protected String getPositionInformation() {
    String result = "";
    if (!hasErrorPosition())
      return result;


    for (SimpleError error : getSimpleErrors()) {
      result += "\nLine: " + error.line + " Character: " + (error.character);
      result += " " + error.message;
    }

    return result;
  }
  
  public List<SimpleError> getSimpleErrors() {
    List<SimpleError> result = new ArrayList<SimpleError>();
    
    for (AntlrException ex : errors) {
      result.add(new SimpleError(ex.getLine(), ex.getCharacter(), ex.getMessage()));
    }
    
    return result;
  }

  public static class SimpleError {
    private final int line;
    private final int character;
    private final String message;

    public SimpleError(int line, int character, String message) {
      super();
      this.line = line;
      this.character = character;
      this.message = message;
    }

    public int getLine() {
      return line;
    }

    public int getCharacter() {
      return character;
    }

    public String getMessage() {
      return message;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + character;
      result = prime * result + line;
      result = prime * result + ((message == null) ? 0 : message.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      SimpleError other = (SimpleError) obj;
      if (character != other.character)
        return false;
      if (line != other.line)
        return false;
      if (message == null) {
        if (other.message != null)
          return false;
      } else if (!message.equals(other.message))
        return false;
      return true;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append(line);
      builder.append(":");
      builder.append(character);
      builder.append(":");
      builder.append(message);
      return builder.toString();
    }
    
  }
}
