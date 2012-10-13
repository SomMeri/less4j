package com.github.sommeri.less4j.core.compiler;

import com.github.sommeri.less4j.core.ast.PureMixin;

public class MixinWithVariablesState {
  private final PureMixin mixin;
  private final VariablesScope variablesUponDefinition;

  public MixinWithVariablesState(PureMixin mixin, VariablesScope variablesUponDefinition) {
    super();
    this.mixin = mixin;
    this.variablesUponDefinition = variablesUponDefinition;
  }

  public PureMixin getMixin() {
    return mixin;
  }

  public VariablesScope getVariablesUponDefinition() {
    return variablesUponDefinition;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("MixinWithVariablesState [mixin=");
    builder.append(mixin.getName());
    builder.append(", variablesUponDefinition=");
    builder.append(variablesUponDefinition);
    builder.append("]");
    return builder.toString();
  }

}