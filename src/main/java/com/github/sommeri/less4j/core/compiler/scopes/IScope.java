package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.compiler.scopes.refactoring.ILocalScope;
import com.github.sommeri.less4j.core.compiler.scopes.refactoring.SurroundingScopes;

public interface IScope extends ILocalScope {

  public IScope getParent();

  public List<IScope> getChilds();

  public boolean hasParent();

  public String toString();

  public String toFullName();

  //TODO: (!!!) skoncila som tu
  public IScope firstChild();

  public IScope skipBodyOwner();

  public String toLongString();

  public void setParent(IScope parent);

  public void removedFromAst();

  public boolean isPresentInAst();

  public IScope getRootScope();

  public boolean seesLocalDataOf(IScope otherScope);

  public IScope getChildOwnerOf(ASTCssNode body);

  public IScope childByOwners(ASTCssNode headNode, ASTCssNode... restNodes);

  public int getTreeSize();

  //FIXME (!!!) these used to be private
  public StringBuilder toLongString(int level);

  public void addChild(IScope child);

  public ASTCssNode getOwner();

  //FIXME (!!!) these did not exists at all 
  public ILocalScope getLocalScope();

  public Expression getLocalValue(Variable variable);

  public Expression getLocalValue(String name);

  //FIXME (!!!) this MUST be removed 
  @Deprecated
  public SurroundingScopes getSurroundingScopes();

}
