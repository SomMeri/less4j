package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.ArrayList;
import java.util.Iterator;

import com.github.sommeri.less4j.core.problems.BugHappened;

public class IteratedScope {

  private final IScope scope;
  private Iterator<IScope> childsIterator;

  public IteratedScope(IScope scope) {
    super();
    this.scope = scope;
    childsIterator = (new ArrayList<IScope>(scope.getChilds())).iterator();
  }

  public IScope getScope() {
    return scope;
  }

  public IteratedScope getNextChild() {
    IScope child = null;
    do {
      if (childsIterator.hasNext())
        child = childsIterator.next();
      else 
        throw new BugHappened("Child scope was expected but missing", getScope().getOwner());
    } while (!child.isPresentInAst());
    
    return new IteratedScope(child);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("IteratedScope [scope=");
    builder.append(scope);
    builder.append("]");
    return builder.toString();
  }
  
  
}
