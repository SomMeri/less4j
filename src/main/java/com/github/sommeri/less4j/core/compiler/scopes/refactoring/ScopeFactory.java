package com.github.sommeri.less4j.core.compiler.scopes.refactoring;

import java.util.ArrayList;
import java.util.Arrays;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.LocalScope;

public class ScopeFactory {

  private static final String DEFAULT = "#default#";
  private static final String SCOPE = "#scope#";
  //FIXME: (!) remove and clean up
  public static final String BODY_OWNER = "#body-owner#";
  private static final String DUMMY = "#dummy#";

  public static IScope createDefaultScope(ASTCssNode owner) {
    return new BasicScope(new LocalScope(owner, new ArrayList<String>(), DEFAULT), new SurroundingScopes());
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
    return new BasicScope(new LocalScope(owner, Arrays.asList(name), DUMMY), new SurroundingScopes());
  }

  private static IScope createScope(ASTCssNode owner, IScope parent, String type) {
    BasicScope result = new BasicScope(new LocalScope(owner, new ArrayList<String>(), type), new SurroundingScopes());
    result.setParent(parent);
    return result;
  }

}
