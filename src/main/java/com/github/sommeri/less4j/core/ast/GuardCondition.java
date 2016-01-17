package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class GuardCondition extends Guard {
  
  private Expression condition;
  
  public GuardCondition(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  public GuardCondition(HiddenTokenAwareTree underlyingStructure, Expression condition) {
    super(underlyingStructure);
    this.condition = condition;
  }

  public Expression getCondition() {
    return condition;
  }

  public void setCondition(Expression condition) {
    this.condition = condition;
  }

  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(condition);
  }

  @Override
  public Type getGuardType() {
    return Guard.Type.CONDITION ;
  }

  @Override
  public GuardCondition clone() {
    GuardCondition result = (GuardCondition) super.clone();
    result.condition = condition==null?null:condition.clone();
    result.configureParentToAllChilds();
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("[");
    builder.append(condition).append("]");
    return builder.toString();
  }

}
