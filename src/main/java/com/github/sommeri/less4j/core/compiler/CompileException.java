package com.github.sommeri.less4j.core.compiler;

import com.github.sommeri.less4j.core.ast.ASTCssNode;

@SuppressWarnings("serial")
public class CompileException extends RuntimeException {

  private ASTCssNode node;

  public CompileException(ASTCssNode node) {
    super();
    this.node = node;
  }

  public CompileException(String message, Throwable th, ASTCssNode node) {
    super(message, th);
    this.node = node;
  }

  public CompileException(Throwable th, ASTCssNode node) {
    super(th);
    this.node = node;
  }

  public CompileException(String message, ASTCssNode node) {
    super(message);
    this.node = node;
  }

  @Override
  public String getMessage() {
    return super.getMessage() + getPositionInformation();
  }

  private String getPositionInformation() {
    if (node==null)
      return "";
    
    String result = "\n Line: "  + node.getSourceLine() + " Character: " + node.getCharPositionInSourceLine();
    return result;
  }

  
}
