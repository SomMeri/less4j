package com.github.sommeri.less4j.core.compiler.scopes.refactoring;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.ScopeView;

public class SurroundingScopesJointParentView extends SurroundingScopesView {

  private AbstractScopeView additionalChild;

  public SurroundingScopesJointParentView(ISurroundingScopes originalStructure, AbstractScopeView publicParent, AbstractScopeView additionalChild) {
    super(originalStructure, publicParent, null);
    this.additionalChild = additionalChild;
  }

  @Override
  public List<IScope> createPublicChilds() {
    List<IScope> result = new ArrayList<IScope>();
    for (IScope childScope : originalStructure.getChilds()) {
      result.add(new ScopeView(childScope, owner, null));
    }

    result.add(additionalChild);
    return result;
  }

}
