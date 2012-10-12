package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class GuardCondition extends ASTCssNode {
  
  private boolean isNegated;
  private Expression condition;
  
  public GuardCondition(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  public GuardCondition(HiddenTokenAwareTree underlyingStructure, boolean isNegated, Expression condition) {
    super(underlyingStructure);
    this.isNegated = isNegated;
    this.condition = condition;
  }

  public boolean isNegated() {
    return isNegated;
  }

  public void setNegated(boolean isNegated) {
    this.isNegated = isNegated;
  }

  public Expression getCondition() {
    return condition;
  }

  public void setCondition(Expression condition) {
    this.condition = condition;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(condition);
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.GUARD_CONDITION;
  }

  @Override
  public GuardCondition clone() {
    GuardCondition result = (GuardCondition) super.clone();
    result.condition = condition==null?null:condition.clone();
    result.configureParentToAllChilds();
    return result;
  }

}
