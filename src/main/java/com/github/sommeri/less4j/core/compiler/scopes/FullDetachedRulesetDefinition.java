package com.github.sommeri.less4j.core.compiler.scopes;

import com.github.sommeri.less4j.core.ast.DetachedRuleset;

public class FullDetachedRulesetDefinition {
  private final DetachedRuleset detached;
  private final IScope bodyScope;

  public FullDetachedRulesetDefinition(DetachedRuleset detached, IScope mixinsBodyScope) {
    super();
    this.detached = detached;
    this.bodyScope = mixinsBodyScope;
  }

  public DetachedRuleset getDetached() {
    return detached;
  }

  public IScope getScope() {
    return bodyScope;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("FullDetachedDefinition [detached [");
    builder.append(detached.getSourceLine()).append(":").append(detached.getSourceColumn());
    builder.append("], bodyScope=");
    builder.append(bodyScope);
    builder.append("]");
    return builder.toString();
  }

}