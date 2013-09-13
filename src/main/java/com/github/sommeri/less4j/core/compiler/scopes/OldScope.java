package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.compiler.scopes.refactoring.BasicScope;
import com.github.sommeri.less4j.core.compiler.scopes.refactoring.SurroundingScopes;

public class OldScope extends BasicScope implements IScope {

  public OldScope(String type, ASTCssNode owner, List<String> names, IScope parent) {
    super(new LocalScope(owner, names, type), new SurroundingScopes());
    setParent(parent);
  }

  public OldScope(String type, ASTCssNode owner, List<String> names, IScope parent, LocalScopeData initialLocalData) {
    super(new LocalScope(owner, initialLocalData, names, type), new SurroundingScopes());
    setParent(parent);
  }

  public OldScope(String type, ASTCssNode owner, String name, IScope parent) {
    this(type, owner, new ArrayList<String>(Arrays.asList(name)), parent);
  }

  public OldScope(String type, ASTCssNode owner, List<String> names) {
    this(type, owner, names, null);
  }

  protected OldScope(String type, ASTCssNode owner, String name) {
    this(type, owner, new ArrayList<String>(Arrays.asList(name)), null);
  }

}
