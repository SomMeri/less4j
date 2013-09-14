package com.github.sommeri.less4j.core.compiler.scopes;


public abstract class AbstractScopesTree implements IScopesTree {

  @Override
  public boolean hasParent() {
    return getParent() != null;
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
