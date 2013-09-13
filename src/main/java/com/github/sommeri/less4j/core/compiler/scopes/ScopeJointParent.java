package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.ArrayList;
import java.util.List;

public class ScopeJointParent extends ScopeView {

  private ScopeView additionalChild;
  private IScope decoree;

  public ScopeJointParent(IScope decoree, ScopeView additionalChild) {
    super(decoree, null);
    this.decoree = decoree;
    this.additionalChild = additionalChild;
  }

  @Override
  protected List<IScope> createPublicChilds() {
    List<IScope> result = new ArrayList<IScope>();
    for (IScope childScope : decoree.getChilds()) {
      result.add(createChildScopeView(childScope));
    }

    result.add(additionalChild);
    return result;
  }
}
