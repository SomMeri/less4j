package com.github.sommeri.less4j.core.compiler.scopes;

import com.github.sommeri.less4j.core.ast.PureMixin;

public class FullMixinDefinition {
  private final PureMixin mixin;
  private final Scope bodyScope;

  public FullMixinDefinition(PureMixin mixin, Scope mixinsBodyScope) {
    super();
    this.mixin = mixin;
    this.bodyScope = mixinsBodyScope;
  }

  public PureMixin getMixin() {
    return mixin;
  }

  public Scope getScope() {
    return bodyScope;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("FullMixinDefinition [mixin=");
    builder.append(mixin.getName());
    builder.append(", bodyScope=");
    builder.append(bodyScope);
    builder.append("]");
    return builder.toString();
  }

}