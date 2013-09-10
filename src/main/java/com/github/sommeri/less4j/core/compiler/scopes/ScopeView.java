package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScopeView extends ScopeDecorator {

  private Scope publicParent = null;
  private List<Scope> publicChilds = null;

  private Map<Scope, ScopeView> fakeChildsMap = new HashMap<Scope, ScopeView>();
  private Scope decoree;

  public ScopeView(Scope decoree) {
    super(decoree);
    this.decoree = decoree;
  }

  @Override
  public Scope getParent() {
    if (publicParent != null)
      return publicParent;

    publicParent = createPublicParent();
    return publicParent;
  }

  protected Scope createPublicParent() {
    Scope realParent = super.getParent();
    if (realParent == null)
      return null;
    
    return createParentScopeView(realParent, decoree, this);
  }

  protected ScopeView createParentScopeView(Scope realParent, Scope realChild, ScopeView fakeChild) {
    ScopeView result = new ScopeView(realParent);
    result.fakeChildsMap.put(realChild, fakeChild);

    return result;
  }

  @Override
  public List<Scope> getChilds() {
    if (publicChilds != null)
      return publicChilds;

    publicChilds = createPublicChilds();
    return publicChilds;
  }

  protected List<Scope> createPublicChilds() {
    List<Scope> realChilds = super.getChilds();
    if (realChilds == null)
      return null;

    List<Scope> result = new ArrayList<Scope>();
    for (Scope childScope : realChilds) {
      if (fakeChildsMap.containsKey(childScope)) {
        result.add(fakeChildsMap.get(childScope));
      } else {
        result.add(createChildScopeView(childScope));
      }
    }
    
    return result;

  }

  protected Scope createChildScopeView(Scope realChild) {
    ScopeView result = new ScopeView(realChild);
    result.publicParent = this;

    return result;
  }

}