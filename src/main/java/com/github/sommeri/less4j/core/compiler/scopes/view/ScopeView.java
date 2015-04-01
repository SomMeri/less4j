package com.github.sommeri.less4j.core.compiler.scopes.view;

import com.github.sommeri.less4j.core.compiler.scopes.BasicScope;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.IScopesTree;
import com.github.sommeri.less4j.core.compiler.scopes.local.SaveableLocalScope;

public class ScopeView extends BasicScope {

  private IScope underlying;
  public SaveableLocalScope saveableLocalScope;

  public ScopeView(IScope underlying, IScopesTree surroundingScopes) {
    super(new SaveableLocalScope(underlying.getLocalScope()), surroundingScopes);
    this.underlying = underlying;
    this.saveableLocalScope = getLocalScope();
  }

  public void toIndependentWorkingCopy() {
    this.saveableLocalScope.save();
  }

  public void toIndependentWorkingCopyAllParents() {
    //System.out.println(" +++ saving: " + this);
    this.saveableLocalScope.save();
    
    if (hasParent()) {
      getParent().toIndependentWorkingCopyAllParents();
    }
  }

  @Override
  public ScopeView getParent() {
    return (ScopeView)getSurroundingScopes().getParent();
  }

  @Override
  public SaveableLocalScope getLocalScope() {
    return (SaveableLocalScope) super.getLocalScope();
  }

  public IScope getUnderlying() {
    return underlying;
  }

}
