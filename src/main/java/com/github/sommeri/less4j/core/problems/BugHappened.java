package com.github.sommeri.less4j.core.problems;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ProblemsPrinter.AbsoluteSourceNamePrinter;

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

    AbsoluteSourceNamePrinter printer = new AbsoluteSourceNamePrinter();
    String filename = printer.printSourceName(offendingNode.getSource());
    
    return "\n Offending place: " + filename + " " + offendingNode.getSourceLine() + ":" + offendingNode.getSourceColumn();
  }
  
  private static String errorPlace(HiddenTokenAwareTree offendingNode) {
    if (offendingNode==null)
      return "";

    AbsoluteSourceNamePrinter printer = new AbsoluteSourceNamePrinter();
    String filename = printer.printSourceName(offendingNode.getSource());
    
    return "\n Offending place: " + filename + " " + offendingNode.getLine() + ":" + (offendingNode.getCharPositionInLine() + 1);
  }
  
}
