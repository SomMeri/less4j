package com.github.sommeri.less4j.core.compiler.scopes;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.compiler.scopes.refactoring.ILocalScope;
import com.github.sommeri.less4j.core.compiler.scopes.refactoring.ISurroundingScopes;

public interface IScope extends ILocalScope, ISurroundingScopes {

  public String toString();

  public String toFullName();

  //TODO: (!!!) skoncila som tu
  public IScope firstChild();

  public IScope skipBodyOwner();

  public String toLongString();

  public void removedFromAst();

  public boolean isPresentInAst();

  public IScope getRootScope();

  public boolean seesLocalDataOf(IScope otherScope);

  public IScope getChildOwnerOf(ASTCssNode body);

  public IScope childByOwners(ASTCssNode headNode, ASTCssNode... restNodes);

  //FIXME (!!!) these used to be private
  public StringBuilder toLongString(int level);

  public ASTCssNode getOwner();

  //FIXME (!!!) these did not exists at all 
  public ILocalScope getLocalScope();

  public Expression getValue(Variable variable);

  public Expression getValue(String name);

  public Expression getLocalValue(Variable variable);

  public Expression getLocalValue(String name);

  public ISurroundingScopes getSurroundingScopes();

}
