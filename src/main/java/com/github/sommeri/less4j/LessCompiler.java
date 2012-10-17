package com.github.sommeri.less4j;


public interface LessCompiler {

  public String compile(String lessContent) throws Less4jException;

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
