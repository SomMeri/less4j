package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.ArrayList;
import java.util.List;

public class ScopeJointParent extends ScopeView {

  private ScopeView additionalChild;
  private Scope decoree;

  public ScopeJointParent(Scope decoree, ScopeView additionalChild) {
    super(decoree, null);
    this.decoree = decoree;
    this.additionalChild = additionalChild;
  }

  @Override
  protected List<Scope> createPublicChilds() {
    List<Scope> result = new ArrayList<Scope>();
    for (Scope childScope : decoree.getChilds()) {
      result.add(createChildScopeView(childScope));
    }

    result.add(additionalChild);
    return result;
  }
}
