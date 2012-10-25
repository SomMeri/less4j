package com.github.sommeri.less4j.core.compiler.remove;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Deprecated
public class NamespaceTree {

  private final String name;
  private final NamespaceTree parent;
  private List<NamespaceTree> childs = new ArrayList<NamespaceTree>();
  private MixinsScopeOld mixinsScope;

  public NamespaceTree(String name, NamespaceTree parent, MixinsScopeOld mixinsScope) {
    super();
    this.name = name;
    this.parent = parent;
    this.mixinsScope = mixinsScope;
    if (parent != null)
      parent.addChild(this);
  }

  public NamespaceTree(String name, NamespaceTree parent) {
    this(name, parent, null);
  }

  public NamespaceTree(String name) {
    this(name, null, null);
  }

  private void addChild(NamespaceTree child) {
    childs.add(child);
  }

  public NamespaceTree getParent() {
    return parent;
  }

  public MixinsScopeOld getMixinsScope() {
    return mixinsScope;
  }

  public List<NamespaceTree> findMatchingChilds(List<String> nameChain) {
    if (nameChain.isEmpty())
      return Arrays.asList(this);
    
    String fistName = nameChain.get(0);
    List<String> theRest = nameChain.subList(1, nameChain.size()); 
    List<NamespaceTree> result = new ArrayList<NamespaceTree>();
    for (NamespaceTree kid : childs) {
      if (kid.getName().equals(fistName)) {
        result.addAll(kid.findMatchingChilds(theRest));
      }
    }
    return result;
  }

  private String getName() {
    return name;
  }

  public boolean hasParent() {
    return parent!=null;
  }

  @Override
  public String toString() {
    if (hasParent())
      return getParent() + " > " + getName();
    return getName();
  }

  public void setScope(MixinsScopeOld scope) {
    this.mixinsScope = scope;
  }

  public boolean hasScope() {
    return mixinsScope!=null;
  }

}
