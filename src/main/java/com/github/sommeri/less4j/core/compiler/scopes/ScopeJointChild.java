package com.github.sommeri.less4j.core.compiler.scopes;

//FIXME: (!!!) this needs to be refactored and interfaced
public class ScopeJointChild extends ScopeView {

  private Scope parent;
  private Scope current;
  
  protected ScopeJointChild(Scope parent, Scope current) {
    //super("#joined#", null, first.getNames() +" > "+ second.getNames());
    super(current);
    this.parent = parent;
    this.current = current;
  }

  @Override
  protected Scope createPublicParent() {
    return new ScopeJointParent(parent, this);
  }

}

