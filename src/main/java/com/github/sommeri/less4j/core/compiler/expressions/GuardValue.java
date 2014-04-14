package com.github.sommeri.less4j.core.compiler.expressions;

import com.github.sommeri.less4j.core.compiler.stages.MixinCompilationResult;
import com.github.sommeri.less4j.utils.ArraysUtils.Filter;

public enum GuardValue {
  USE, DO_NOT_USE, USE_IF_DEFAULT, USE_IF_NOT_DEFAULT;

  public Filter<MixinCompilationResult> filter() {
    return new DefaultFunctionUseFilter(this);
  }
}

class DefaultFunctionUseFilter implements Filter<MixinCompilationResult> {

  private final GuardValue value;

  public DefaultFunctionUseFilter(GuardValue value) {
    super();
    this.value = value;
  }

  @Override
  public boolean accept(MixinCompilationResult t) {
    return t.getGuardValue().equals(value);
  }

}