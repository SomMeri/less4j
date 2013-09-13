package com.github.sommeri.less4j.core.compiler.scopes.refactoring;

import java.util.ArrayList;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.OldScope;

public class ScopeFactory {

  private static final String DEFAULT = "#default#";
  private static final String SCOPE = "#scope#";
  //FIXME: (!) remove and clean up
  public static final String BODY_OWNER = "#body-owner#";
  private static final String DUMMY = "#dummy#";

  public static IScope createDefaultScope(ASTCssNode owner) {
    return new OldScope(DEFAULT, owner, new ArrayList<String>());
  }

  public static IScope createScope(ASTCssNode owner, IScope parent) {
    return new OldScope(SCOPE, owner, new ArrayList<String>(), parent);
  }

  public static IScope createBodyOwnerScope(ASTCssNode owner, IScope parent) {
    return new OldScope(BODY_OWNER, owner, new ArrayList<String>(), parent);
  }

  public static IScope createDummyScope() {
    return new OldScope(DUMMY, null, new ArrayList<String>(), null);
  }

  public static IScope createDummyScope(ASTCssNode owner, String name) {
    return new OldScope(DUMMY, owner, name, null);
  }

}
