package com.github.sommeri.less4j.core.compiler.scopes.refactoring;

import java.util.Iterator;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.AbstractVariableDeclaration;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.ReusableStructureName;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionFilter;
import com.github.sommeri.less4j.core.compiler.scopes.FullMixinDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.LocalScopeData;
import com.github.sommeri.less4j.core.compiler.scopes.MixinsDefinitionsStorage;
import com.github.sommeri.less4j.core.compiler.scopes.VariablesDeclarationsStorage;

public class BasicScope implements IScope {

  // scope data - where should this belong to?
  private boolean presentInAst = true;

  private ILocalScope localScope;
  private SurroundingScopes surroundingScopes;

  public BasicScope(ILocalScope localScope, SurroundingScopes tree) {
    this.localScope = localScope;
    this.surroundingScopes = tree;
  }

  public SurroundingScopes getSurroundingScopes() {
    return surroundingScopes;
  }

  public void setSurroundingScopes(SurroundingScopes surroundingScopes) {
    this.surroundingScopes = surroundingScopes;
  }

  public ILocalScope getLocalScope() {
    return localScope;
  }

  /* ******************************************************************** */
  public IScope getParent() {
    return surroundingScopes.getParent();
  }

  public void addChild(IScope child) {
    surroundingScopes.addChild(child);
  }

  public List<IScope> getChilds() {
    return surroundingScopes.getChilds();
  }

  public int getTreeSize() {
    return surroundingScopes.getTreeSize();
  }

  /* ******************************************************************** */
  public IScope firstChild() {
    return getChilds().get(0);
  }

  public boolean hasParent() {
    return getParent() != null;
  }

  public IScope childByOwners(ASTCssNode headNode, ASTCssNode... restNodes) {
    IScope head = getChildOwnerOf(headNode);
    if (head == null)
      return null;

    for (ASTCssNode node : restNodes) {
      head = head.getChildOwnerOf(node);
      if (head == null)
        return null;
    }

    return head;
  }

  public IScope getChildOwnerOf(ASTCssNode body) {
    for (IScope kid : getChilds()) {
      if (kid.getOwner() == body)
        return kid;
    }
    return null;
  }

  public boolean seesLocalDataOf(IScope otherScope) {
    if (getLocalScope().hasTheSameLocalData(otherScope.getLocalScope()))
      return true;

    if (!hasParent())
      return false;

    return getParent().seesLocalDataOf(otherScope);
  }


  public IScope getRootScope() {
    if (!hasParent())
      return this;

    return getParent().getRootScope();
  }

  public IScope skipBodyOwner() {
    if (isBodyOwnerScope())
      return firstChild().skipBodyOwner();

    return this;
  }

  /* ******************************************************************** */

  public Expression getValue(Variable variable) {
    return getValue(variable.getName());
  }
  
  public Expression getValue(String name) {
    Expression value = getLocalValue(name);
    
    if (value == null && hasParent())
      value = getParent().getValue(name);
    
    return value;
  }


  /* ******************************************************************** */
  public void setLocalScope(ILocalScope localScope) {
    this.localScope = localScope;
  }

  public void addNames(List<String> names) {
    localScope.addNames(names);
  }

  public List<String> getNames() {
    return localScope.getNames();
  }

  public ASTCssNode getOwner() {
    return localScope.getOwner();
  }

  public String getType() {
    return localScope.getType();
  }

  public boolean hasTheSameLocalData(ILocalScope otherScope) {
    return localScope.hasTheSameLocalData(otherScope);
  }

  public void registerVariable(AbstractVariableDeclaration declaration) {
    localScope.registerVariable(declaration);
  }

  public void registerVariable(AbstractVariableDeclaration node, Expression replacementValue) {
    localScope.registerVariable(node, replacementValue);
  }

  public void registerVariableIfNotPresent(String name, Expression replacementValue) {
    localScope.registerVariableIfNotPresent(name, replacementValue);
  }

  public void registerVariable(String name, Expression replacementValue) {
    localScope.registerVariable(name, replacementValue);
  }

  public void addFilteredVariables(ExpressionFilter filter, IScope source) {
    localScope.addFilteredVariables(filter, source);
  }

  public void registerMixin(ReusableStructure mixin, IScope mixinsBodyScope) {
    localScope.registerMixin(mixin, mixinsBodyScope);
  }

  public void createPlaceholder() {
    localScope.createPlaceholder();
  }

  public void addToPlaceholder(IScope otherScope) {
    localScope.addToPlaceholder(otherScope);
  }

  public void closePlaceholder() {
    localScope.closePlaceholder();
  }

  public void addAllMixins(List<FullMixinDefinition> mixins) {
    localScope.addAllMixins(mixins);
  }

  public void add(IScope otherSope) {
    localScope.add(otherSope);
  }

  public void addVariables(IScope otherSope) {
    localScope.addVariables(otherSope);
  }

  public List<FullMixinDefinition> getAllMixins() {
    return localScope.getAllMixins();
  }

  public List<FullMixinDefinition> getMixinsByName(List<String> nameChain, ReusableStructureName name) {
    return localScope.getMixinsByName(nameChain, name);
  }

  public List<FullMixinDefinition> getMixinsByName(ReusableStructureName name) {
    return localScope.getMixinsByName(name);
  }

  public List<FullMixinDefinition> getMixinsByName(String name) {
    return localScope.getMixinsByName(name);
  }

  public MixinsDefinitionsStorage getLocalMixins() {
    return localScope.getLocalMixins();
  }

  public VariablesDeclarationsStorage getLocalVariables() {
    return localScope.getLocalVariables();
  }

  public LocalScopeData getLocalData() {
    return localScope.getLocalData();
  }

  public void createLocalDataSnapshot() {
    localScope.createLocalDataSnapshot();
  }

  public void discardLastLocalDataSnapshot() {
    localScope.discardLastLocalDataSnapshot();
  }

  public boolean isBodyOwnerScope() {
    return localScope.isBodyOwnerScope();
  }

  public Expression getLocalValue(Variable variable) {
    return localScope.getValue(variable);
  }

  public Expression getLocalValue(String name) {
    return localScope.getValue(name);
  }

  /* ******************************************************************** */

  public void removedFromAst() {
    presentInAst = false;
  }

  public boolean isPresentInAst() {
    return presentInAst;
  }

  public void setParent(IScope parent) {
    if (getSurroundingScopes().hasParent()) {
      getParent().getChilds().remove(this);
    }

    getSurroundingScopes().setParent(parent);

    if (parent != null)
      parent.addChild(this);
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder(toFullName());
    result.append("\n\n").append(super.toString());
    return result.toString();
  }

  public String toFullName() {
    if (hasParent())
      return getParent().toFullName() + " > " + toSimpleName();

    return toSimpleName().toString();
  }

  private String toSimpleName() {
    List<String> names = getNames();
    return "" + getType() + names;
  }

  public String toLongString() {
    return toLongString(0).toString();
  }

  public StringBuilder toLongString(int level) {
    String prefix = "";
    for (int i = 0; i < level; i++) {
      prefix += "  ";
    }
    StringBuilder text = new StringBuilder(prefix);

    Iterator<String> iNames = getNames().iterator();
    text.append(iNames.next());
    while (iNames.hasNext()) {
      text.append(", ").append(iNames.next());
    }

    text.append("(").append(getLocalVariables().size()).append(", ").append(getLocalMixins().size());
    text.append(") {").append("\n");

    for (IScope kid : getChilds()) {
      text.append(kid.toLongString(level + 1));
    }
    text.append(prefix).append("}").append("\n");
    return text;
  }

}
