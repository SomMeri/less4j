package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.sommeri.less4j.core.compiler.scopes.FullMixinDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.ScopeFactory;
import com.github.sommeri.less4j.core.compiler.scopes.view.ScopeView;

public class CallerCalleeScopeJoiner {

  public ScopeView joinIfIndependentAndPreserveContent(IScope callerScope, IScope bodyScope) {
    // locally defined mixin does not require any other action
    boolean isLocalImport = bodyScope.seesLocalDataOf(callerScope); 

    ScopeView result = null;
    if (isLocalImport) {
      // we need to copy the whole tree, because this runs inside referenced mixin scope 
      // snapshot and imported mixin needs to remember the scope as it is now 
      result = ScopeFactory.createJoinedScopesView(null, bodyScope);
    } else {
      // since this is non-local import, we need to join reference scope and imported mixins scope
      // imported mixin needs to have access to variables defined in caller
      result = ScopeFactory.createJoinedScopesView(callerScope, bodyScope);
    }
    result.saveLocalDataForTheWholeWayUp();
    return result;
  }
  
  public IScope joinIfIndependent(IScope callerScope, IScope bodyScope) {
    // locally defined mixin does not require any other action
    boolean isLocallyDefined = bodyScope.seesLocalDataOf(callerScope); 

    if (isLocallyDefined) {
      return bodyScope;
    }

    //join scopes
    IScope result = ScopeFactory.createJoinedScopesView(callerScope, bodyScope);
    return result;
  }

  public List<FullMixinDefinition> mixinsToImport(IScope callerScope, IScope calleeScope, List<FullMixinDefinition> unmodifiedMixinsToImport) {
    List<FullMixinDefinition> result = new ArrayList<FullMixinDefinition>();
    for (FullMixinDefinition mixinToImport : unmodifiedMixinsToImport) {
      boolean isLocalImport = mixinToImport.getScope().seesLocalDataOf(callerScope);
      ScopeView newScope = null;
      if (isLocalImport) {
        // we need to copy the whole tree, because this runs inside referenced mixin scope 
        // snapshot and imported mixin needs to remember the scope as it is now 
        newScope = ScopeFactory.createJoinedScopesView(null, mixinToImport.getScope());
        newScope.saveLocalDataForTheWholeWayUp();
      } else {
        // since this is non-local import, we need to join reference scope and imported mixins scope
        // imported mixin needs to have access to variables defined in caller
        newScope = ScopeFactory.createJoinedScopesView(calleeScope, mixinToImport.getScope());
        newScope.saveLocalDataForTheWholeWayUp();
      }

      result.add(new FullMixinDefinition(mixinToImport.getMixin(), newScope));
    }
    return result;
  }

  public IScope createJoinedScopes(List<IScope> parents, IScope child) {
    // this could and maybe should check whether they are local to each other e.g., whther they see each other
    // if they are local, parent scope could be skipped
    // such logic is not implemented yet
    Iterator<IScope> iterator = parents.iterator();
    if (!iterator.hasNext())
      return child;

    IScope result = iterator.next();
    while (iterator.hasNext()) {
      IScope next = iterator.next();
      result = ScopeFactory.createJoinedScopesView(result, next);
    }
    result = ScopeFactory.createJoinedScopesView(result, child);

    return result;
  }
}
