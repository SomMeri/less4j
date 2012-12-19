package com.github.sommeri.less4j.core.compiler.scopes;

import com.github.sommeri.less4j.core.ast.ReusableStructure;

public class FullMixinDefinition {
  private final ReusableStructure mixin;
  private final Scope bodyScope;

  public FullMixinDefinition(ReusableStructure mixin, Scope mixinsBodyScope) {
    super();
    this.mixin = mixin;
    this.bodyScope = mixinsBodyScope;
  }

  public ReusableStructure getMixin() {
    return mixin;
  }

  public Scope getScope() {
    return bodyScope;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("FullMixinDefinition [mixin=");
    builder.append(mixin.getNames().get(0)).append(" ...");
    builder.append(", bodyScope=");
    builder.append(bodyScope);
    builder.append("]");
    return builder.toString();
  }

}