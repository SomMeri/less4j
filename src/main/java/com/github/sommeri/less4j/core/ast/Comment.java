package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

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

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return Collections.emptyList();
  }
  
  @Override
  public Comment clone() {
    return (Comment) super.clone();
  }
}
