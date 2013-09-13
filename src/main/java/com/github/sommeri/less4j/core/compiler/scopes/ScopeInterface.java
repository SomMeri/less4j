package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.AbstractVariableDeclaration;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionFilter;

public interface ScopeInterface {

  public void addNames(List<String> names);

  public IScope getParent();

  public List<IScope> getChilds();

  public List<String> getNames();

  public boolean hasParent();

  public String toString();

  public String toFullName();

  public void registerVariable(AbstractVariableDeclaration declaration);

  public void registerVariable(AbstractVariableDeclaration node, Expression replacementValue);

  public void registerVariableIfNotPresent(String name, Expression replacementValue);

  public void registerVariable(String name, Expression replacementValue);

  public void fillByFilteredVariables(ExpressionFilter filter, IScope source);

  public void addAllMixins(List<FullMixinDefinition> mixins);

  public void add(IScope otherSope);

  //FIXME: (!!!) ugly
  @Deprecated
  public void addVariables(IScope otherSope);

  public Expression getValue(Variable variable);

  public Expression getValue(String name);

  //FIXME: (!!!) crime against programming, but I'm in prototype phase
  @Deprecated
  public Expression getVariableValueDoNotRegister(String name);

  public Expression getLocalValue(Variable variable);

  public Expression getLocalValue(String name);

  public void registerMixin(ReusableStructure mixin, IScope mixinsBodyScope);

  public void createPlaceholder();

  public void addToPlaceholder(IScope otherScope);

  public void closePlaceholder();

  public IScope firstChild();

  public boolean isBodyOwnerScope();

  public IScope skipBodyOwner();

  public String toLongString();

  public void setParent(IScope parent);

  public void removedFromTree();

  public boolean isPresentInTree();

  public IScope getRootScope();

  public boolean seesLocalDataOf(IScope otherScope);

  public IScope getChildOwnerOf(ASTCssNode body);

  public IScope childByOwners(ASTCssNode headNode, ASTCssNode... restNodes);

  public int getTreeSize();

}