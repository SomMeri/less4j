package com.github.sommeri.less4j.core.compiler.scopes;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Variable;

//FIXME: !!!! TODO: add   @Override to the whole hierarchy and maybe clean up again
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

  public FullExpressionDefinition getLocalValue(Variable variable);

  public FullExpressionDefinition getLocalValue(String name);

  // smart setters 
  public void setParentKeepConsistency(IScope parent);

  // internals access 
  public IScopesTree getSurroundingScopes();

  public ILocalScope getLocalScope();

}
