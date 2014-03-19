package com.github.sommeri.less4j;

import com.github.sommeri.less4j.core.ast.ASTCssNode;

public interface LessProblems {
  
  public void addError(ASTCssNode errorNode, String description);

  public void addWarning(ASTCssNode weirdNode, String description);
  
}