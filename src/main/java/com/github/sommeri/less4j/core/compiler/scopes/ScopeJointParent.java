package com.github.sommeri.less4j.core.compiler.scopes;

import com.github.sommeri.less4j.core.compiler.scopes.refactoring.AbstractScopeView;
import com.github.sommeri.less4j.core.compiler.scopes.refactoring.SaveableLocalScope;
import com.github.sommeri.less4j.core.compiler.scopes.refactoring.SurroundingScopesJointParentView;
import com.github.sommeri.less4j.core.compiler.scopes.refactoring.SurroundingScopesView;


public class ScopeJointParent extends AbstractScopeView {

  public ScopeJointParent(IScope underlying, AbstractScopeView publicParent, AbstractScopeView additionalChild) {
    super(new SaveableLocalScope(underlying.getLocalScope()), new SurroundingScopesJointParentView(underlying.getSurroundingScopes(), publicParent, additionalChild), null);
    ((SurroundingScopesView)super.getSurroundingScopes()).setOwner(this);
    this.underlying = underlying;
  }

}
