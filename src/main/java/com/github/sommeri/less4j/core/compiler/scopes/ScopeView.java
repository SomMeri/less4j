package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.Variable;

public class ScopeView extends ScopeDecorator {

  private ScopeView publicParent = null;
  private List<Scope> publicChilds = null;

  private Map<Scope, ScopeView> fakeChildsMap = new HashMap<Scope, ScopeView>();
  private Scope decoree;
  private Scope joinToParentTree;
  private LocalScopeData fakeLocalData;

  public ScopeView(Scope decoree, Scope joinToParentTree) {
    super(decoree);
    this.decoree = decoree;
    this.joinToParentTree = joinToParentTree;
  }

  public void saveLocalDataForTheWholeWayUp() {
    this.fakeLocalData = decoree.getLocalData().clone();
    if (hasParent())
      getParent().saveLocalDataForTheWholeWayUp();
  }

  @Override
  public ScopeView getParent() {
    if (publicParent != null)
      return publicParent;

    publicParent = createPublicParent();
    return publicParent;
  }

  public boolean hasParent() {
    return getParent()!=null;
  }

  protected ScopeView createPublicParent() {
    Scope realParent = super.getParent();
    if (realParent != null)
      return createParentScopeView(realParent, decoree, this);

    if (joinToParentTree==null)
      return null;
    
    return new ScopeJointParent(joinToParentTree, this);
  }

  protected ScopeView createParentScopeView(Scope realParent, Scope realChild, ScopeView fakeChild) {
    ScopeView result = new ScopeView(realParent, joinToParentTree);
    result.fakeChildsMap.put(realChild, fakeChild);

    return result;
  }

  @Override
  public List<Scope> getChilds() {
    if (publicChilds != null)
      return publicChilds;

    publicChilds = createPublicChilds();
    return publicChilds;
  }

  protected List<Scope> createPublicChilds() {
    List<Scope> realChilds = super.getChilds();
    if (realChilds == null)
      return null;

    List<Scope> result = new ArrayList<Scope>();
    for (Scope childScope : realChilds) {
      if (fakeChildsMap.containsKey(childScope)) {
        result.add(fakeChildsMap.get(childScope));
      } else {
        result.add(createChildScopeView(childScope));
      }
    }
    
    return result;

  }

  protected Scope createChildScopeView(Scope realChild) {
    ScopeView result = new ScopeView(realChild, joinToParentTree);
    result.publicParent = this;

    return result;
  }

  public Scope getRootScope() {
    if (!hasParent())
      return this;

    return getParent().getRootScope();
  }
  
  //FIXME: (!!!) well split and clean up
  public Expression getValue(String name) {
    if (fakeLocalData!=null) {
      //FIXME: (!!!) wrong and weird, probably incorrect (unsnapshoted version will be searched)
      Expression value = fakeLocalData.getVariables().getValue(name);
      if (value!=null)
        return value;
    }
    Expression value = super.getLocalValue(name);
    if (value!=null || !hasParent())
      return value;
    
    return getParent().getValue(name);
  }

  @Override
  public Expression getValue(Variable variable) {
    System.out.println(variable + " from " + toFullName());
    if (fakeLocalData!=null) {
      //FIXME: (!!!) wrong and weird, probably incorrect (unsnapshoted version will be searched)
      Expression value = fakeLocalData.getVariables().getValue(variable.getName());
      if (value!=null)
        return value;
    }
    Expression value = super.getLocalValue(variable);
    if (value!=null || !hasParent())
      return value;
    
    return getParent().getValue(variable);
  }

  public Expression getLocalValue(Variable variable) {
    if (fakeLocalData!=null) {
      //FIXME: (!!!) wrong and weird, probably incorrect (unsnapshoted version will be searched)
      Expression value = fakeLocalData.getVariables().getValue(variable.getName());
      if (value!=null)
        return value;
    }
    return decoree.getLocalValue(variable);
  }

  public Expression getLocalValue(String name) {
    if (fakeLocalData!=null) {
      //FIXME: (!!!) wrong and weird, probably incorrect (unsnapshoted version will be searched)
      Expression value = fakeLocalData.getVariables().getValue(name);
      if (value!=null)
        return value;
    }
    return decoree.getLocalValue(name);
  }


}