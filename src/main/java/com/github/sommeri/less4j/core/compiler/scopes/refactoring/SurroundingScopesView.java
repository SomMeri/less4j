package com.github.sommeri.less4j.core.compiler.scopes.refactoring;

import java.util.List;

import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.ScopeJointParent;
import com.github.sommeri.less4j.core.compiler.scopes.ScopeView;

public class SurroundingScopesView extends AbstractSurroundingScopes {

  private ScopeView owner;
  private ScopeView publicParent;
  private IScope joinToParentTree;
  private final ISurroundingScopes originalStructure;

  private List<IScope> publicChilds = null;

  public SurroundingScopesView(ISurroundingScopes originalStructure, ScopeView publicParent, IScope joinToParentTree) {
    super();
    this.originalStructure = originalStructure;
    this.joinToParentTree = joinToParentTree;
    this.publicParent = publicParent;
  }

  public void setOwner(ScopeView owner) {
    this.owner = owner;
  }

  @Override
  public void addChild(IScope child) {
    throw new IllegalStateException("Scopes view does not accept new childs.");
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

    publicChilds = owner.createPublicChilds();
    return publicChilds;
  }

  @Override
  public boolean hasParent() {
    return getParent() != null;
  }

  @Override
  public void setParent(IScope parent) {
    throw new IllegalStateException("Scopes view does not accept new parents.");
  }

  protected ScopeView createPublicParent() {
    IScope realParent = originalStructure.getParent();
    if (realParent != null)
      return createParentScopeView(realParent, owner.underlying, owner);

    if (joinToParentTree == null)
      return null;

    return new ScopeJointParent(joinToParentTree, null, owner);
  }

  protected ScopeView createParentScopeView(IScope realParent, IScope realChild, ScopeView fakeChild) {
    ScopeView result = new ScopeView(realParent, null, joinToParentTree);
    result.fakeChildsMap.put(realChild, fakeChild);

    return result;
  }

}
