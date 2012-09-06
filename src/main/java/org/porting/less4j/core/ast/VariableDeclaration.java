package org.porting.less4j.core.ast;

import java.util.List;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;
import org.porting.less4j.utils.ArraysUtils;

public class VariableDeclaration extends ASTCssNode {

  private Variable variable;
  private Expression value;

  public VariableDeclaration(HiddenTokenAwareTree underlyingStructure, Variable variable, Expression value) {
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
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(variable, value);
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.VARIABLE_DECLARATION;
  }

}
