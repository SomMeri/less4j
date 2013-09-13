package com.github.sommeri.less4j.core.compiler.scopes;

import com.github.sommeri.less4j.core.compiler.scopes.refactoring.AbstractScopeView;
import com.github.sommeri.less4j.core.compiler.scopes.refactoring.SaveableLocalScope;
import com.github.sommeri.less4j.core.compiler.scopes.refactoring.SurroundingScopesView;

public class ScopeView extends AbstractScopeView {

  //FIXME (!!!) make this protected, public constructor should not have publicParent argument
  public ScopeView(IScope underlying, AbstractScopeView publicParent, IScope joinToParentTree) {
    super(new SaveableLocalScope(underlying.getLocalScope()), new SurroundingScopesView(underlying.getSurroundingScopes(), publicParent, joinToParentTree), joinToParentTree);
    ((SurroundingScopesView)super.getSurroundingScopes()).setOwner(this);
    this.underlying = underlying;
  }

}