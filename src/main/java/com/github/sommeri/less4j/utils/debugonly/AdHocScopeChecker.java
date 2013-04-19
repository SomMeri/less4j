package com.github.sommeri.less4j.utils.debugonly;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.compiler.scopes.FullMixinDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.Scope;

//marked deprecated so I get a warning if it is referenced somewhere
@Deprecated
public class AdHocScopeChecker {

  public AdHocScopeChecker() {
  }

  public void validate(Scope scope) {
    check(scope);

    List<Scope> childs = new ArrayList<Scope>(scope.getChilds());
    for (Scope kid : childs) {
      validate(kid);
    }

  }

  private void check(Scope scope) {
    for (FullMixinDefinition mixinDefinition : scope.getAllMixins()) {
      Scope mixinScope = mixinDefinition.getScope();
      if (mixinScope == null)
        throw new IllegalStateException("double WTF: mixin definition without scope");
      
      if (mixinScope.getParent()!=scope && grandParent(mixinScope)!=scope)
        throw new IllegalStateException("WTF");
    }
  }

  private Scope grandParent(Scope mixinScope) {
    if (!mixinScope.hasParent())
      return null;
    
    return mixinScope.getParent().getParent();
  }

 }
