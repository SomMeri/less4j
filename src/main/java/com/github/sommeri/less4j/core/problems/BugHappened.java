package com.github.sommeri.less4j.core.problems;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

@SuppressWarnings("serial")
public class BugHappened extends RuntimeException {

  private static final String PREFIX = "Bug, please report issue: ";

  public BugHappened(String message, HiddenTokenAwareTree offendingNode) {
    super(PREFIX + message + errorPlace(offendingNode));
  }

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
  
  private static String errorPlace(HiddenTokenAwareTree offendingNode) {
    if (offendingNode==null)
      return "";
    
    return "\n Offending place: " + offendingNode.getLine() + ":" + (offendingNode.getCharPositionInLine() + 1);
  }
  
}
