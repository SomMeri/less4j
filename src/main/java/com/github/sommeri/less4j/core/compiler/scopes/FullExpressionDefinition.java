package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class FullExpressionDefinition extends Expression {
  
  private final Expression node;
  private final IScope owningScope;

  public FullExpressionDefinition(Expression node, IScope owningScope) {
    super(node.getUnderlyingStructure());
    this.node = node;
    this.owningScope = owningScope;
  }

  public Expression getNode() {
    return node;
  }

  public IScope getOwningScope() {
    return owningScope;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(this.getClass().getSimpleName()).append(" [ [").append(getNode());
    builder.append(getNode().getSourceLine()).append(":").append(getNode().getSourceColumn());
    builder.append("], primaryScope=");
    builder.append(owningScope);
    builder.append("]");
    return builder.toString();
  }

  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(node);
  }

  @Override
  public ASTCssNodeType getType() {
    return null;
  }

}