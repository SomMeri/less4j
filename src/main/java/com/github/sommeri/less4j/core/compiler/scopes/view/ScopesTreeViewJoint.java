package com.github.sommeri.less4j.core.compiler.scopes.view;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.IScopesTree;
import com.github.sommeri.less4j.core.compiler.scopes.ScopeFactory;

public class ScopesTreeViewJoint extends ScopesTreeView {

  private IScope additionalChild;
  private IScopesTree originalStructure;

  public ScopesTreeViewJoint(IScopesTree originalStructure, ScopeView publicParent, IScope additionalChild) {
    super(originalStructure, null, publicParent, null);
    this.originalStructure = originalStructure;
    this.additionalChild = additionalChild;
  }

  @Override
  public List<IScope> createPublicChilds() {
    List<IScope> result = new ArrayList<IScope>();
    for (IScope childScope : originalStructure.getChilds()) {
      result.add(ScopeFactory.createChildScopeView(childScope, scope, null));
    }

    result.add(additionalChild);
    return result;
  }

}
