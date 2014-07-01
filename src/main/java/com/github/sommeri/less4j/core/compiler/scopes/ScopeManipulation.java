package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.compiler.scopes.view.ScopeView;

public class ScopeManipulation {

  //FIXME !!!! refactor and clean, unify with references solver joiner 
  public ScopeView constructImportedBodyScope(IScope importTargetScope, IScope bodyToBeImportedScope) {
    ScopeView newScope = null;
    // locally defined mixin does not require any other action
    boolean isLocalImport = bodyToBeImportedScope.seesAllDataOf(importTargetScope);

    if (isLocalImport) {
      // we need to copy the whole tree, because this runs inside referenced mixin scope 
      // snapshot and imported mixin needs to remember the scope as it is now 
      newScope = ScopeFactory.createJoinedScopesView(null, bodyToBeImportedScope);
      newScope.saveLocalDataForTheWholeWayUp();
    } else {
      // since this is non-local import, we need to join reference scope and imported mixins scope
      // imported mixin needs to have access to variables defined in caller
      newScope = ScopeFactory.createJoinedScopesView(importTargetScope, bodyToBeImportedScope);
      newScope.saveLocalDataForTheWholeWayUp();
    }
    return newScope;
  }
  
  public IScope calculateBodyWorkingScope(IScope callerScope, IScope bodyScope) {
    //FIXME !!!!!! find the case where this is needed for mixins and create test case for it 
    // locally defined mixin does not require any other action
    boolean isLocallyDefined = bodyScope.seesAllDataOf(callerScope);

    if (isLocallyDefined) {
      return bodyScope;
    }

    //join scopes
    IScope result = ScopeFactory.createJoinedScopesView(callerScope, bodyScope);
    return result;
  }

  //FIXME: !!! evaluate need for this
  public IScope calculateMixinsWorkingScope(IScope callerScope, IScope arguments, IScope mixinScope) {
    // add arguments
    IScope mixinDeclarationScope = mixinScope.getParent();
    mixinDeclarationScope.add(arguments);

    // locally defined mixin does not require any other action
    boolean isLocallyDefined = mixinDeclarationScope.seesLocalDataOf(callerScope);
    if (isLocallyDefined) {
      return mixinScope;
    }

    //join scopes
    IScope result = ScopeFactory.createJoinedScopesView(callerScope, mixinScope);
    return result;
  }

  public List<FullMixinDefinition> mixinsToImport(IScope referenceScope, IScope referencedMixinScope, List<FullMixinDefinition> unmodifiedMixinsToImport) {
    List<FullMixinDefinition> result = new ArrayList<FullMixinDefinition>();
    for (FullMixinDefinition mixinToImport : unmodifiedMixinsToImport) {
      boolean isLocalImport = mixinToImport.getScope().seesLocalDataOf(referenceScope);
      if (isLocalImport) {
        // we need to copy the whole tree, because this runs inside referenced mixin scope 
        // snapshot and imported mixin needs to remember the scope as it is now 
        ScopeView newWay = ScopeFactory.createJoinedScopesView(null, mixinToImport.getScope());
        newWay.saveLocalDataForTheWholeWayUp();
        result.add(new FullMixinDefinition(mixinToImport.getMixin(), newWay));
      } else {
        // since this is non-local import, we need to join reference scope and imported mixins scope
        // imported mixin needs to have access to variables defined in caller
        ScopeView newWay = ScopeFactory.createJoinedScopesView(referencedMixinScope, mixinToImport.getScope());
        newWay.saveLocalDataForTheWholeWayUp();
        result.add(new FullMixinDefinition(mixinToImport.getMixin(), newWay));
      }

    }
    return result;
  }

}
