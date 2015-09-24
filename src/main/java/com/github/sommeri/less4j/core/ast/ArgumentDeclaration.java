package com.github.sommeri.less4j.core.ast;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class ArgumentDeclaration extends AbstractVariableDeclaration {
  
  public ArgumentDeclaration(AbstractVariableDeclaration copy) {
    super(copy);
  }

  public ArgumentDeclaration(HiddenTokenAwareTree underlyingStructure, Variable variable, Expression value) {
    super(underlyingStructure, variable, value);
  }

  public ArgumentDeclaration(Variable variable, Expression value) {
    this(variable.getUnderlyingStructure(), variable, value);
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

  public boolean isMandatory() {
    return !hasDefaultValue() && !isCollector();
  }

}
