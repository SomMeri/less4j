package com.github.sommeri.less4j.core.compiler.scopes;

import com.github.sommeri.less4j.core.ast.ASTCssNode;

public class FullNodeDefinition {
  
  private final ASTCssNode node;
  private final IScope primaryScope;

  public FullNodeDefinition(ASTCssNode node, IScope mixinsBodyScope) {
    super();
    this.node = node;
    this.primaryScope = mixinsBodyScope;
  }

  public ASTCssNode getNode() {
    return node;
  }

  public IScope getPrimaryScope() {
    return primaryScope;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("FullDetachedDefinition [detached [");
    builder.append(getNode().getSourceLine()).append(":").append(getNode().getSourceColumn());
    builder.append("], primaryScope=");
    builder.append(primaryScope);
    builder.append("]");
    return builder.toString();
  }

}