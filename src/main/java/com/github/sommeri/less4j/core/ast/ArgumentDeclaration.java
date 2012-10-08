package com.github.sommeri.less4j.core.ast;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class ArgumentDeclaration extends AbstractVariableDeclaration {

  public ArgumentDeclaration(AbstractVariableDeclaration copy) {
    super(copy);
  }

  public ArgumentDeclaration(HiddenTokenAwareTree underlyingStructure, Variable variable, Expression value) {
    super(underlyingStructure, variable, value);
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.ARGUMENT_DECLARATION;
  }

  @Override
  public AbstractVariableDeclaration clone() {
    return (ArgumentDeclaration)super.clone();
  }

  public boolean hasDefaultValue() {
    return getValue()!=null;
  }

}
