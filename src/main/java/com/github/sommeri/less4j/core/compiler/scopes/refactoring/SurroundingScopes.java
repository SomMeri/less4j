package com.github.sommeri.less4j.core.compiler.scopes.refactoring;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.compiler.scopes.IScope;

public class SurroundingScopes implements ISurroundingScopes {

  // tree structure
  private IScope parent;
  private List<IScope> childs = new ArrayList<IScope>();

  public SurroundingScopes() {
  }
  
  public SurroundingScopes(IScope parent) {
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
  public boolean hasParent() {
    return getParent() != null;
  }

  @Override
  public void setParent(IScope parent) {
    this.parent = parent;
  }

  @Override
  public int getTreeSize() {
    int result = 1;
    for (IScope kid : getChilds()) {
      result = result + kid.getTreeSize();
    }
    return result;
  }


}
