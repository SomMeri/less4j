package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.compiler.scopes.refactoring.AbstractScopeView;
import com.github.sommeri.less4j.core.compiler.scopes.refactoring.SaveableLocalScope;
import com.github.sommeri.less4j.core.compiler.scopes.refactoring.SurroundingScopesView;

public class ScopeView extends AbstractScopeView {

  //FIXME (!!!) make this protected, public constructor should not have publicParent argument
  public ScopeView(IScope underlying, ScopeView publicParent, IScope joinToParentTree) {
    super(new SaveableLocalScope(underlying.getLocalScope()), new SurroundingScopesView(underlying.getSurroundingScopes(), publicParent, joinToParentTree), joinToParentTree);
    ((SurroundingScopesView)super.getSurroundingScopes()).setOwner(this);
    this.underlying = underlying;
  }

  public List<IScope> createPublicChilds() {
    List<IScope> realChilds = underlying.getChilds();
    if (realChilds == null)
      return null;

    List<IScope> result = new ArrayList<IScope>();
    for (IScope childScope : realChilds) {
      if (fakeChildsMap.containsKey(childScope)) {
        result.add(fakeChildsMap.get(childScope));
      } else {
        result.add(new ScopeView(childScope, this, joinToParentTree));
      }
    }
    
    return result;

  }

}