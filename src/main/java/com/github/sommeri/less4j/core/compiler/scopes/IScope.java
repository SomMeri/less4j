package com.github.sommeri.less4j.core.compiler.scopes;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.Variable;

public interface IScope extends ILocalScope, IScopesTree {

  // debug printing
  public StringBuilder toLongString(int indentationLevel);

  public String toString();

  public String toFullName();

  public boolean seesLocalDataOf(IScope otherScope);

  //smart util methods
  public IScope firstChild();

  public IScope skipBodyOwner();

  public String toLongString();

  public IScope getRootScope();

  public IScope getChildOwnerOf(ASTCssNode body);

  public IScope childByOwners(ASTCssNode headNode, ASTCssNode... restNodes);

  // data access methods 
  public Expression getValue(Variable variable);

  public Expression getValue(String name);

  public Expression getLocalValue(Variable variable);

  public Expression getLocalValue(String name);

  // smart setters 
  public void setParentKeepConsistency(IScope parent);

  // internals access 
  public IScopesTree getSurroundingScopes();

  public ILocalScope getLocalScope();


}
