package com.github.sommeri.less4j.core;

import com.github.sommeri.less4j.core.ast.ASTCssNode;

@SuppressWarnings("serial")
public class NotACssException extends RuntimeException {

  private final ASTCssNode node;

  public NotACssException(ASTCssNode node) {
    super("Node type: " + node.getType() + " | " + node);
    this.node = node;
  }

  public ASTCssNode getNode() {
    return node;
  }

}
