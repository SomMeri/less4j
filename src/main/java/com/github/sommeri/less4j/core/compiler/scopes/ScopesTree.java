package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.ArrayList;
import java.util.List;


public class ScopesTree extends AbstractScopesTree {

  // tree structure
  private IScope parent;
  private List<IScope> childs = new ArrayList<IScope>();

  public ScopesTree() {
  }
  
  public ScopesTree(IScope parent) {
    setParent(parent);
  }

  @Override
  public void addChild(IScope child) {
    childs.add(child);
  }

  @Override
  public IScope getParent() {
    return parent;
  }

  @Override
  public List<IScope> getChilds() {
    return childs;
  }

  @Override
  public void setParent(IScope parent) {
    this.parent = parent;
  }

}
