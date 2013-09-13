package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.ArrayList;
import java.util.List;

public class ScopeJointParent extends ScopeView {

  private ScopeView additionalChild;
  private IScope decoree;

  public ScopeJointParent(IScope decoree, ScopeView publicParent, ScopeView additionalChild) {
    super(decoree, publicParent, null);
    this.decoree = decoree;
    this.additionalChild = additionalChild;
  }

  @Override
  public List<IScope> createPublicChilds() {
    List<IScope> result = new ArrayList<IScope>();
    for (IScope childScope : decoree.getChilds()) {
      result.add(new ScopeView(childScope, this, joinToParentTree));
    }

    result.add(additionalChild);
    return result;
  }
}
