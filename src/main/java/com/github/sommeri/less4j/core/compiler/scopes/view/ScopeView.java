package com.github.sommeri.less4j.core.compiler.scopes.view;

import com.github.sommeri.less4j.core.compiler.scopes.BasicScope;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.IScopesTree;
import com.github.sommeri.less4j.core.compiler.scopes.local.SaveableLocalScope;

public class ScopeView extends BasicScope {

  private IScope underlying;
  private SaveableLocalScope saveableLocalScope;

  public ScopeView(IScope underlying, IScopesTree surroundingScopes) {
    super(new SaveableLocalScope(underlying.getLocalScope()), surroundingScopes);
    this.underlying = underlying;
    this.saveableLocalScope = getLocalScope();
  }

  public void saveLocalDataForTheWholeWayUp() {
    this.saveableLocalScope.save();
    
    if (hasParent()) {
      getParent().saveLocalDataForTheWholeWayUp();
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
