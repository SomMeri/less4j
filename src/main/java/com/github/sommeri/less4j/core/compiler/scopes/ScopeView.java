package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.sommeri.less4j.core.compiler.scopes.refactoring.AbstractSurroundingScopes;
import com.github.sommeri.less4j.core.compiler.scopes.refactoring.BasicScope;
import com.github.sommeri.less4j.core.compiler.scopes.refactoring.ISurroundingScopes;
import com.github.sommeri.less4j.core.compiler.scopes.refactoring.SaveableLocalScope;

public class ScopeView extends BasicScope {

  private ScopeView publicParent = null;
  private Map<IScope, ScopeView> fakeChildsMap = new HashMap<IScope, ScopeView>();
  
  private IScope underlying;
  private IScope joinToParentTree;
  
  private SurroundingScopesView surroundingScopesView;
  private SaveableLocalScope saveableLocalScope;

  public ScopeView(IScope underlying, IScope joinToParentTree) {
    super(new SaveableLocalScope(underlying.getLocalScope()), underlying.getSurroundingScopes());
    
    this.underlying = underlying;
    this.joinToParentTree = joinToParentTree;
    
    this.surroundingScopesView = new SurroundingScopesView(this, underlying.getSurroundingScopes());
    this.saveableLocalScope = (SaveableLocalScope)super.getLocalScope();
  }

  //FIXME: (!!!) functional but hack
  public void saveLocalDataForTheWholeWayUp() {
    this.saveableLocalScope.save();
    
    if (hasParent()) {
      getParent().saveLocalDataForTheWholeWayUp();
    }
  }

  @Override
  public ScopeView getParent() {
    return getSurroundingScopes().getParent();
  }

  protected List<IScope> createPublicChilds() {
    List<IScope> realChilds = super.getChilds();
    if (realChilds == null)
      return null;

    List<IScope> result = new ArrayList<IScope>();
    for (IScope childScope : realChilds) {
      if (fakeChildsMap.containsKey(childScope)) {
        result.add(fakeChildsMap.get(childScope));
      } else {
        result.add(createChildScopeView(childScope));
      }
    }
    
    return result;

  }

  protected IScope createChildScopeView(IScope realChild) {
    ScopeView result = new ScopeView(realChild, joinToParentTree);
    result.publicParent = this;

    return result;
  }

  public SurroundingScopesView getSurroundingScopes() {
    return surroundingScopesView;
  }

  class SurroundingScopesView extends AbstractSurroundingScopes {
    
    private final ScopeView owner;
    private final ISurroundingScopes originalStructure;

    private List<IScope> publicChilds = null;

    public SurroundingScopesView(ScopeView owner, ISurroundingScopes originalStructure) {
      super();
      this.owner = owner;
      this.originalStructure = originalStructure;
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

      publicChilds = createPublicChilds();
      return publicChilds;
    }

    @Override
    public boolean hasParent() {
      return getParent()!=null;
    }

    @Override
    public void setParent(IScope parent) {
      throw new IllegalStateException("Scopes view does not accept new parents.");
    }

    protected ScopeView createPublicParent() {
      IScope realParent = originalStructure.getParent();
      if (realParent != null)
        return createParentScopeView(realParent, underlying, owner);

      if (joinToParentTree==null)
        return null;
      
      return new ScopeJointParent(joinToParentTree, owner);
    }

    protected ScopeView createParentScopeView(IScope realParent, IScope realChild, ScopeView fakeChild) {
      ScopeView result = new ScopeView(realParent, joinToParentTree);
      result.fakeChildsMap.put(realChild, fakeChild);

      return result;
    }

  }

}