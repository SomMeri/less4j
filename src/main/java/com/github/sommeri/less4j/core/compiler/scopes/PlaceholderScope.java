package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.Collections;
import java.util.List;


public class PlaceholderScope extends BasicScope {

  private final DataPlaceholder placeholder;

  public PlaceholderScope(IScope parent, ILocalScope localScope, IScopesTree surroundingScopes) {
    super(localScope, surroundingScopes);
    super.setParentKeepConsistency(parent);
    placeholder = parent.createDataPlaceholder();
  }

  public void setParentKeepConsistency(IScope parent) {
    throw new IllegalStateException("Placeholder should never be reparented.");
  }

  public void replaceSelf(IScope by) {
    IScope parent = getParent();
    replaceChild(parent, this, by.getChilds());
    parent.replacePlaceholder(placeholder, by);
  }

  public void removeSelf() {
    List<IScope> nothing = Collections.emptyList();
    replaceChild(getParent(), this, nothing);  
  }

}
