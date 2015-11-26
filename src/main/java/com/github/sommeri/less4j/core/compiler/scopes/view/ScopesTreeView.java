package com.github.sommeri.less4j.core.compiler.scopes.view;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.compiler.scopes.AbstractScopesTree;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.IScopesTree;
import com.github.sommeri.less4j.core.compiler.scopes.ScopeFactory;

public class ScopesTreeView extends AbstractScopesTree {

  protected ScopeView scope;

  private IScope joinToParentTree;
  private final IScopesTree originalStructure;

  private ScopeView publicParent;
  private List<IScope> publicChilds = null;
  private IScope fakeChildScope;
  private ScopeView fakeChildScopeView;

  public ScopesTreeView(IScopesTree originalStructure, IScope joinToParentTree, ScopeView publicParent, ScopeView publicChild) {
    super();
    this.originalStructure = originalStructure;
    this.joinToParentTree = joinToParentTree;
    this.publicParent = publicParent;

    if (publicChild != null) {
      fakeChildScope = publicChild.getUnderlying();
      fakeChildScopeView = publicChild;
    }
  }

  public void setScope(ScopeView scope) {
    this.scope = scope;
  }

  @Override
  public void addChild(IScope child) {
    throw new IllegalStateException("Scopes view does not accept new childs.");
  }

  @Override
  public void setParent(IScope parent) {
    throw new IllegalStateException("Scopes view does not accept new parents.");
  }

  @Override
  public ScopeView getParent() {
    if (publicParent != null)
      return publicParent;

    publicParent = createPublicParent();
    return publicParent;
  }

  @Override
  public List<IScope> getChilds() {
    if (publicChilds != null)
      return publicChilds;

    publicChilds = createPublicChilds();
    return publicChilds;
  }

  private List<IScope> createPublicChilds() {
    List<IScope> realChilds = originalStructure.getChilds();
    if (realChilds == null)
      return null;

    List<IScope> result = new ArrayList<IScope>();
    for (IScope childScope : realChilds) {
      if (fakeChildScope != null && fakeChildScope.equals(childScope)) {
        result.add(fakeChildScopeView);
      } else {
        result.add(ScopeFactory.createChildScopeView(childScope, scope, joinToParentTree));
      }
    }

    return result;

  }

  protected ScopeView createPublicParent() {
    IScope realParent = originalStructure.getParent();
    if (realParent != null)
      return ScopeFactory.createParentScopeView(realParent, scope, joinToParentTree);

    if (joinToParentTree == null)
      return null;

    return ScopeFactory.createScopeViewJoint(joinToParentTree, scope);
  }

}
