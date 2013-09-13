package com.github.sommeri.less4j.core.compiler.scopes.refactoring;

import java.util.HashMap;
import java.util.Map;

import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.ScopeView;

public class AbstractScopeView extends BasicScope {

  public IScope underlying;
  public IScope joinToParentTree;
  
  private SaveableLocalScope saveableLocalScope;
  public Map<IScope, ScopeView> fakeChildsMap = new HashMap<IScope, ScopeView>();

  public AbstractScopeView(SaveableLocalScope localScope, ISurroundingScopes surroundingScopes, IScope joinToParentTree) {
    super(localScope, surroundingScopes);
    this.saveableLocalScope = localScope;
    this.joinToParentTree = joinToParentTree;
  }

  //FIXME: (!!!) functional but hack
  public void saveLocalDataForTheWholeWayUp() {
    this.saveableLocalScope.save();
    
    if (hasParent()) {
      getParent().saveLocalDataForTheWholeWayUp();
    }
  }

  @Override
  public AbstractScopeView getParent() {
    return (AbstractScopeView)getSurroundingScopes().getParent();
  }

}
