package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class VariableNamePart extends InterpolableNamePart {

  private Variable variable;
  
  public VariableNamePart(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  public VariableNamePart(HiddenTokenAwareTree underlyingStructure, Variable variable) {
    this(underlyingStructure);
    this.variable = variable;
  }

  public Variable getVariable() {
    return variable;
  }

  public String getName() {
    return getVariable().getName();
  }
  
  public void setVariable(Variable variable) {
    this.variable = variable;
  }

  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(variable);
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.VARIABLE_NAME_PART;
  }

  public VariableNamePart clone() {
    return (VariableNamePart) super.clone();
  }

}
