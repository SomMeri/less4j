package com.github.sommeri.less4j.core.compiler;

import java.util.Map;

import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.PureMixin;

public class MixinWithScope {
  private final PureMixin mixin;
  private final Map<String, Expression> variablesUponDefinition;

  public MixinWithScope(PureMixin mixin, Map<String, Expression> variablesUponDefinition) {
    super();
    this.mixin = mixin;
    this.variablesUponDefinition = variablesUponDefinition;
  }

  public PureMixin getMixin() {
    return mixin;
  }

  public Map<String, Expression> getVariablesUponDefinition() {
    return variablesUponDefinition;
  }

}