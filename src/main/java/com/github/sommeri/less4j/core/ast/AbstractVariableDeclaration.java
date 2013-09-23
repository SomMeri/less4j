package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public abstract class AbstractVariableDeclaration extends ASTCssNode {

  private Variable variable;
  private Expression value;

  public AbstractVariableDeclaration(AbstractVariableDeclaration copy) {
    this(copy.getUnderlyingStructure(), copy.getVariable(), copy.getValue());
  }

  public AbstractVariableDeclaration(HiddenTokenAwareTree underlyingStructure, Variable variable, Expression value) {
    super(underlyingStructure);
    this.variable = variable;
    this.value = value;
  }

  public Variable getVariable() {
    return variable;
  }

  public void setVariable(Variable variable) {
    this.variable = variable;
  }

  public Expression getValue() {
    return value;
  }

  public void setValue(Expression value) {
    this.value = value;
  }

  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(variable, value);
  }

  @Override
  public String toString() {
    return "" +variable + ": " + value;
  }

  @Override
  public AbstractVariableDeclaration clone() {
    AbstractVariableDeclaration clone = (AbstractVariableDeclaration) super.clone();
    clone.variable = variable==null? null : variable.clone();
    clone.value = value==null? null : value.clone();
    clone.configureParentToAllChilds();
    return clone;
  }

  
  
}
