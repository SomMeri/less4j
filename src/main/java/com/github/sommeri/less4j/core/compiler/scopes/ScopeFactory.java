package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.ArrayList;
import java.util.Arrays;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.compiler.scopes.local.LocalScope;
import com.github.sommeri.less4j.core.compiler.scopes.view.ScopeView;
import com.github.sommeri.less4j.core.compiler.scopes.view.ScopesTreeView;
import com.github.sommeri.less4j.core.compiler.scopes.view.ScopesTreeViewJoint;

public class ScopeFactory {

  public static final String DEFAULT = "#default#";
  public static final String SCOPE = "#scope#";
  public static final String BODY_OWNER = "#body-owner#";
  public static final String DUMMY = "#dummy#";

  public static IScope createDefaultScope(ASTCssNode owner) {
    return new BasicScope(new LocalScope(owner, new ArrayList<String>(), DEFAULT), new ScopesTree());
  }

  public static IScope createScope(ASTCssNode owner, IScope parent) {
    return createScope(owner, parent, SCOPE);
  }

  public static IScope createBodyOwnerScope(ASTCssNode owner, IScope parent) {
    return createScope(owner, parent, BODY_OWNER);
  }

  public static IScope createDummyScope() {
    return createScope(null, null, DUMMY);
  }

  public static IScope createDummyScope(ASTCssNode owner, String name) {
    return new BasicScope(new LocalScope(owner, Arrays.asList(name), DUMMY), new ScopesTree());
  }

  private static IScope createScope(ASTCssNode owner, IScope parent, String type) {
    BasicScope result = new BasicScope(new LocalScope(owner, new ArrayList<String>(), type), new ScopesTree());
    result.setParent(parent);
    return result;
  }

  public static ScopeView createJoinedScopesView(IScope parent, IScope underlyingChild) {
    return createChildScopeView(underlyingChild, null, parent);  
  }
  
  public static ScopeView createChildScopeView(IScope underlying, ScopeView publicParent, IScope joinToParentTree) {
    ScopesTreeView scopesTree = new ScopesTreeView(underlying.getSurroundingScopes(), joinToParentTree, publicParent, null);
    ScopeView result = new ScopeView(underlying, scopesTree);
    scopesTree.setScope(result);
    return result;
  }
  
  public static ScopeView createParentScopeView(IScope underlying, ScopeView fakeChild, IScope joinToParentTree) {
    ScopesTreeView scopesTree = new ScopesTreeView(underlying.getSurroundingScopes(), joinToParentTree, null, fakeChild);
    ScopeView result = new ScopeView(underlying, scopesTree);
    scopesTree.setScope(result);

    return result;
  }

  public static ScopeView createScopeViewJoint(IScope underlyingParent, IScope additionalChild) {
    ScopesTreeView scopesTree = new ScopesTreeViewJoint(underlyingParent.getSurroundingScopes(), null, additionalChild);
    ScopeView result = new ScopeView(underlyingParent, scopesTree);
    scopesTree.setScope(result);
    
    return result;
  }
}
