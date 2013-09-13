package com.github.sommeri.less4j.core.compiler.scopes.refactoring;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.compiler.scopes.IScope;

public class SurroundingScopes {

  //FIXME: (!!!) rename to `presentInAst`
  // scope data
  private boolean presentInTree = true;

  // tree structure
  private IScope parent;
  private List<IScope> childs = new ArrayList<IScope>();

  public SurroundingScopes() {
  }
  
  public SurroundingScopes(IScope parent) {
    setParent(parent);
  }

  public void addChild(IScope child) {
    childs.add(child);
  }

  public IScope getParent() {
    return parent;
  }

  public List<IScope> getChilds() {
    return childs;
  }

  public boolean hasParent() {
    return getParent() != null;
  }

  public void setParent(IScope parent) {
    this.parent = parent;
  }

  public void removedFromTree() {
    presentInTree = false;
  }

  public boolean isPresentInTree() {
    return presentInTree;
  }

  public int getTreeSize() {
    int result = 1;
    for (IScope kid : getChilds()) {
      result = result + kid.getTreeSize();
    }
    return result;
  }


}
