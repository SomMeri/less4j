package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class GuardNegated extends Guard {

  private boolean isNegated;
  private Guard guard;

  public GuardNegated(HiddenTokenAwareTree underlyingStructure, Guard guard) {
    this(underlyingStructure, true, guard);
  }

  public GuardNegated(HiddenTokenAwareTree token, boolean isNegated, Guard guard) {
    super(token);
    this.isNegated = isNegated;
    this.guard = guard;
  }

  public boolean isNegated() {
    return isNegated;
  }

  public void setNegated(boolean isNegated) {
    this.isNegated = isNegated;
  }

  public Guard getGuard() {
    return guard;
  }

  public void setGuard(Guard guard) {
    this.guard = guard;
  }

  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(guard);
  }

  @Override
  public Guard.Type getGuardType() {
    return Guard.Type.NEGATED;
  }

  @Override
  public GuardNegated clone() {
    GuardNegated result = (GuardNegated) super.clone();
    result.guard = guard == null ? null : guard.clone();
    result.configureParentToAllChilds();
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("[");
    builder.append(isNegated()?"!":"").append(guard).append("]");
    return builder.toString();
  }

}
