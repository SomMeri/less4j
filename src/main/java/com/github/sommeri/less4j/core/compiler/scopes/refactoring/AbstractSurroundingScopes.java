package com.github.sommeri.less4j.core.compiler.scopes.refactoring;

import com.github.sommeri.less4j.core.compiler.scopes.IScope;

public abstract class AbstractSurroundingScopes implements ISurroundingScopes {

  @Override
  public int getTreeSize() {
    int result = 1;
    for (IScope kid : getChilds()) {
      result = result + kid.getTreeSize();
    }
    return result;
  }


}
