package com.github.sommeri.less4j.core;

import com.github.sommeri.less4j.core.ast.ASTCssNode;

@SuppressWarnings("serial")
public class NotACssException extends RuntimeException {

  private final ASTCssNode node;

  public NotACssException(ASTCssNode node) {
    super("Bug happened: compilation result contains less features. Node type: " + node.getType() + " Coordinates: "+ node.getSourceLine() +":" + node.getCharPositionInSourceLine() +"| " + node);
    this.node = node;
  }

  public ASTCssNode getNode() {
    return node;
  }

}
