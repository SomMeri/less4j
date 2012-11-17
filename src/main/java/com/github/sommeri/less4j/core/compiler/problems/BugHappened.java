package com.github.sommeri.less4j.core.compiler.problems;

import com.github.sommeri.less4j.core.ast.ASTCssNode;

@SuppressWarnings("serial")
public class BugHappened extends RuntimeException {

  private static final String PREFIX = "Bug, please report issue: ";

  public BugHappened(String message, ASTCssNode offendingNode) {
    super(PREFIX + message + errorPlace(offendingNode));
  }

  public BugHappened(Throwable th, ASTCssNode object) {
    super(PREFIX + errorPlace(object), th);
  }

  private static String errorPlace(ASTCssNode offendingNode) {
    if (offendingNode==null)
      return "";
    
    return "\n Offending place: " + offendingNode.getSourceLine() + ":" + offendingNode.getCharPositionInSourceLine();
  }
  
}
