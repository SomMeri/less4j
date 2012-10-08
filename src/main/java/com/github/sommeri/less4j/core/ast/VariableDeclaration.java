package com.github.sommeri.less4j.core.ast;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class VariableDeclaration extends AbstractVariableDeclaration {

  public VariableDeclaration(AbstractVariableDeclaration copy) {
    super(copy);
  }

  public VariableDeclaration(HiddenTokenAwareTree underlyingStructure, Variable variable, Expression value) {
    super(underlyingStructure, variable, value);
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.VARIABLE_DECLARATION;
  }

  @Override
  public VariableDeclaration clone() {
    VariableDeclaration clone = (VariableDeclaration) super.clone();
    return clone;
  }
}
