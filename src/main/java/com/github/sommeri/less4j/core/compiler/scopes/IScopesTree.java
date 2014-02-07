package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.List;


public interface IScopesTree {

  public void addChild(IScope child);

  public IScope getParent();

  public List<IScope> getChilds();

  public boolean hasParent();

  public void setParent(IScope parent);

  public int getTreeSize();
  
}