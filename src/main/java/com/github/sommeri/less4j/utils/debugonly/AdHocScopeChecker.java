package com.github.sommeri.less4j.utils.debugonly;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.compiler.scopes.FullMixinDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;

//marked deprecated so I get a warning if it is referenced somewhere
@Deprecated
public class AdHocScopeChecker {

  public AdHocScopeChecker() {
  }

  public void validate(IScope scope) {
    check(scope);

    List<IScope> childs = new ArrayList<IScope>(scope.getChilds());
    for (IScope kid : childs) {
      validate(kid);
    }

  }

  private void check(IScope scope) {
    for (FullMixinDefinition mixinDefinition : scope.getAllMixins()) {
      IScope mixinScope = mixinDefinition.getScope();
      if (mixinScope == null)
        throw new IllegalStateException("double WTF: mixin definition without scope");
      
      if (mixinScope.getParent()!=scope && grandParent(mixinScope)!=scope)
        throw new IllegalStateException("WTF");
    }
  }

  private IScope grandParent(IScope mixinScope) {
    if (!mixinScope.hasParent())
      return null;
    
    return mixinScope.getParent().getParent();
  }

 }
