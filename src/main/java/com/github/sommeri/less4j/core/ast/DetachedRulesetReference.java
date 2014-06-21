package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class DetachedRulesetReference extends ASTCssNode {

  private Variable variable;
  
  public DetachedRulesetReference(HiddenTokenAwareTree token, Variable variable) {
    super(token);
    this.variable = variable;
  }

  public Variable getVariable() {
    return variable;
  }

  public void setVariable(Variable variable) {
    this.variable = variable;
  }

  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    List<ASTCssNode> result = ArraysUtils.asNonNullList((ASTCssNode)variable);
    return result;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.DETACHED_RULESET_REFERENCE;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("Reference[");
    builder.append(variable).append("]");
    return builder.toString();
  }
  
  @Override
  public DetachedRulesetReference clone() {
    DetachedRulesetReference result = (DetachedRulesetReference) super.clone();
    result.variable = variable == null ? null : variable.clone();
    result.configureParentToAllChilds();
    return result;
  }

}
