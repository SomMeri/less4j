package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.List;

public class PlaceholderScope extends BasicScope  {

  private final DataPlaceholder placeholder;

  public PlaceholderScope(IScope parent, ILocalScope localScope, IScopesTree surroundingScopes) {
    super(localScope, surroundingScopes);
    super.setParent(parent);
    placeholder = parent.createDataPlaceholder();
  }

  public void setParent(IScope parent) {
    throw new IllegalStateException("Placeholder should never be reparented.");
  }
  
  public void replaceSelf(IScope by) {
    IScope parent = getParent();
    replaceChild(parent, this, by.getChilds());
    parent.addToDataPlaceholder(placeholder, by);
  }

  private void replaceChild(IScope parent, IScope child, List<IScope> replacements) {
    List<IScope> inList = parent.getChilds();
    int indexOf = inList.indexOf(child);
    inList.remove(indexOf);
    inList.addAll(indexOf, replacements);
    
    for (IScope kid : replacements) {
      kid.setParent(parent);  
    }
  }

}
