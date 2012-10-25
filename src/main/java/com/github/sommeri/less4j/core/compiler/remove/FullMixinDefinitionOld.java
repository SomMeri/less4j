package com.github.sommeri.less4j.core.compiler.remove;

import org.codehaus.groovy.ast.VariableScope;

import com.github.sommeri.less4j.core.ast.PureMixin;
import com.github.sommeri.less4j.core.compiler.scopes.VariablesScope;

public class FullMixinDefinitionOld {
  private final PureMixin mixin;
  private final VariablesScope ownerScope;

  public FullMixinDefinitionOld(PureMixin mixin, VariablesScope ownerScope) {
    super();
    this.mixin = mixin;
    this.ownerScope = ownerScope;
  }

  public PureMixin getMixin() {
    return mixin;
  }

  public VariablesScope getVariablesUponDefinition() {
    return ownerScope;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("FullMixinDefinition [mixin=");
    builder.append(mixin.getName());
    builder.append(", ownerScope=");
    builder.append(ownerScope);
    builder.append("]");
    return builder.toString();
  }

}