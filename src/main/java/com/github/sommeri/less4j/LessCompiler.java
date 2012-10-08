package com.github.sommeri.less4j;

import java.util.List;

public interface LessCompiler {

  public String compile(String lessContent);

  //TODO: Warning: untested unfinished feature
  public List<IProblem> getProblems();
  
  public interface IProblem {

    public int getLine();

    public int getCharacter();

    public String getMessage();
    
  }
  
}
