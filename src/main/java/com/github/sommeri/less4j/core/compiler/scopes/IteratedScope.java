package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.ArrayList;
import java.util.Iterator;


public class IteratedScope {

  private final Scope scope;
  private Iterator<Scope> childsIterator;

  public IteratedScope(Scope scope) {
    super();
    this.scope = scope;
    childsIterator = (new ArrayList<Scope>(scope.getChilds())).iterator();
  }

  public Scope getScope() {
    return scope;
  }

  public Scope getNextChild() {
    return childsIterator.next();
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
