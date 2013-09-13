package com.github.sommeri.less4j.core.compiler.scopes.refactoring;

import java.util.List;

import com.github.sommeri.less4j.core.compiler.scopes.IScope;

public interface ISurroundingScopes {

  public void addChild(IScope child);

  public IScope getParent();

  public List<IScope> getChilds();

  public boolean hasParent();

  public void setParent(IScope parent);

  public int getTreeSize();

}