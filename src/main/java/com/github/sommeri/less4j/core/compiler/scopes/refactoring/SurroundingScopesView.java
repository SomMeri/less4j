package com.github.sommeri.less4j.core.compiler.scopes.refactoring;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.ScopeJointParent;
import com.github.sommeri.less4j.core.compiler.scopes.ScopeView;

public class SurroundingScopesView extends AbstractSurroundingScopes {

  //FIXME: (!!!) turn to private
  protected IScope joinToParentTree;
  protected AbstractScopeView owner;
  protected AbstractScopeView publicParent;
  protected final ISurroundingScopes originalStructure;

  private List<IScope> publicChilds = null;

  public SurroundingScopesView(ISurroundingScopes originalStructure, AbstractScopeView publicParent, IScope joinToParentTree) {
    super();
    this.originalStructure = originalStructure;
    this.joinToParentTree = joinToParentTree;
    this.publicParent = publicParent;
  }

  public void setOwner(AbstractScopeView owner) {
    this.owner = owner;
  }

  @Override
  public void addChild(IScope child) {
    throw new IllegalStateException("Scopes view does not accept new childs.");
  }

  @Override
  public AbstractScopeView getParent() {
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

  public List<IScope> createPublicChilds() {
    List<IScope> realChilds = originalStructure.getChilds();
    if (realChilds == null)
      return null;

    List<IScope> result = new ArrayList<IScope>();
    for (IScope childScope : realChilds) {
      if (owner.fakeChildsMap.containsKey(childScope)) {
        result.add(owner.fakeChildsMap.get(childScope));
      } else {
        result.add(new ScopeView(childScope, owner, joinToParentTree));
      }
    }
    
    return result;

  }
  @Override
  public void setParent(IScope parent) {
    throw new IllegalStateException("Scopes view does not accept new parents.");
  }

  protected AbstractScopeView createPublicParent() {
    IScope realParent = originalStructure.getParent();
    if (realParent != null)
      return createParentScopeView(realParent, owner.underlying, owner);

    if (joinToParentTree == null)
      return null;

    return new ScopeJointParent(joinToParentTree, null, owner);
  }

  protected ScopeView createParentScopeView(IScope realParent, IScope realChild, AbstractScopeView fakeChild) {
    ScopeView result = new ScopeView(realParent, null, joinToParentTree);
    result.fakeChildsMap.put(realChild, fakeChild);

    return result;
  }

}
