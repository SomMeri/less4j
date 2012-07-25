package org.porting.less4j.core.ast;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

public class Comment extends ASTCssNode {

  private String comment;
  private boolean hasNewLine;
  
  public Comment(HiddenTokenAwareTree underlyingStructure) {
    this(underlyingStructure, underlyingStructure.getText(), false);
  }
  
  public Comment(HiddenTokenAwareTree underlyingStructure, String comment, boolean hasNewLine) {
    super(underlyingStructure);
    this.comment = comment;
    this.hasNewLine = hasNewLine;
  }
  
  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }
  
  public boolean hasNewLine() {
    return hasNewLine;
  }

  public void setHasNewLine(boolean hasNewLine) {
    this.hasNewLine = hasNewLine;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.COMMENT;
  }

  @Override
  public String toString() {
    return getClass().getName() + " [comment=" + comment + ", hasNewLine=" + hasNewLine + "]";
  }
  
}
