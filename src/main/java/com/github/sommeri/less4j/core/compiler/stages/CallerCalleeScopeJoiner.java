package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.compiler.scopes.FullMixinDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.ScopeFactory;
import com.github.sommeri.less4j.core.compiler.scopes.view.ScopeView;

public class CallerCalleeScopeJoiner {

  public ScopeView joinIfIndependentAndPreserveContent(IScope callerScope, IScope bodyScope) {
    ScopeView result = joinIfIndependent(callerScope, bodyScope);
    result.toIndependentWorkingCopyAllParents();
    return result;
  }
  
  public ScopeView joinIfIndependent(IScope callerScope, IScope bodyScope) {
    // locally defined mixin does not require any other action
    boolean isLocallyDefined = isLocallyDefined(callerScope, bodyScope);

    if (isLocallyDefined) {
      return ScopeFactory.createSaveableView(bodyScope);
    }

    //join scopes
    ScopeView result = ScopeFactory.createJoinedScopesView(callerScope, bodyScope);
    return result;
  }

  private boolean isLocallyDefined(IScope callerScope, IScope bodyScope) {
    boolean isLocallyDefined = true;
    
    IScope checkScope = callerScope;
    while (isLocallyDefined && checkScope!=null) {
      isLocallyDefined = bodyScope.seesLocalDataOf(checkScope);
      checkScope = checkScope.getParent();
    }
    return isLocallyDefined;
  }

  public List<FullMixinDefinition> mixinsToImport(IScope callerScope, IScope calleeScope, List<FullMixinDefinition> unmodifiedMixinsToImport) {
    List<FullMixinDefinition> result = new ArrayList<FullMixinDefinition>();
    for (FullMixinDefinition mixinToImport : unmodifiedMixinsToImport) {
      boolean isLocalImport = isLocallyDefined(calleeScope, mixinToImport.getScope());//why caller and not callee??? FIXME !!!!!!!!!!!!
      ScopeView newScope = null;
      if (isLocalImport) {
        // we need to copy the whole tree, because this runs inside referenced mixin scope 
        // snapshot and imported mixin needs to remember the scope as it is now 
        newScope = ScopeFactory.createSaveableView(mixinToImport.getScope());
      } else {
        // since this is non-local import, we need to join reference scope and imported mixins scope
        // imported mixin needs to have access to variables defined in caller
        newScope = ScopeFactory.createJoinedScopesView(calleeScope, mixinToImport.getScope());
      }
      newScope.toIndependentWorkingCopyAllParents();
      result.add(new FullMixinDefinition(mixinToImport.getMixin(), newScope));
    }
    return result;
  }

}
