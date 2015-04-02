package com.github.sommeri.less4j.core.compiler.stages;

import com.github.sommeri.less4j.core.ast.BodyOwner;
import com.github.sommeri.less4j.core.compiler.expressions.GuardValue;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.view.ScopeView;

public class BodyCompilationData {

  private BodyOwner<?> compiledBodyOwner;
  private ScopeView mixinWorkingScope;
  private GuardValue guardValue;
  private IScope arguments;

  public BodyCompilationData(BodyOwner<?> compiledBodyOwner) {
    this.compiledBodyOwner = compiledBodyOwner;
  }

  public void setGuardValue(GuardValue guardValue) {
    this.guardValue = guardValue;
  }

  public GuardValue getGuardValue() {
    return guardValue;
  }

  public BodyOwner<?> getCompiledBodyOwner() {
    return compiledBodyOwner;
  }

  public void setCompiledBodyOwner(BodyOwner<?> compiledBodyOwner) {
    this.compiledBodyOwner = compiledBodyOwner;
  }
  
  public ScopeView getMixinWorkingScope() {
    return mixinWorkingScope;
  }

  public void setMixinWorkingScope(ScopeView mixinWorkingScope) {
    this.mixinWorkingScope = mixinWorkingScope;
  }

  @Override
  protected BodyCompilationData clone() {
    return new BodyCompilationData(compiledBodyOwner.clone());
  }
  
  public void setArguments(IScope arguments) {
    this.arguments = arguments;
  }

  public IScope getArguments() {
    return arguments;
  }


}