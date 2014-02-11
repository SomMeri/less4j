package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.AbstractVariableDeclaration;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.ReusableStructureName;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionFilter;
import com.github.sommeri.less4j.core.compiler.scopes.local.LocalScopeData;
import com.github.sommeri.less4j.core.compiler.scopes.local.MixinsDefinitionsStorage;
import com.github.sommeri.less4j.core.compiler.scopes.local.VariablesDeclarationsStorage;

public abstract class ComposedDumbScope implements ILocalScope, IScopesTree {
  
  private ILocalScope localScope;
  private IScopesTree surroundingScopes;

  public ComposedDumbScope(ILocalScope localScope, IScopesTree surroundingScopes) {
    this.localScope = localScope;
    this.surroundingScopes = surroundingScopes;
  }

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

  public DataPlaceholder createDataPlaceholder() {
    return localScope.createDataPlaceholder();
  }

  public void addToDataPlaceholder(IScope otherScope) {
    localScope.addToDataPlaceholder(otherScope);
  }

  public void replacePlaceholder(DataPlaceholder placeholder, IScope otherScope) {
    localScope.replacePlaceholder(placeholder, otherScope);
  }

  public void closeDataPlaceholder() {
    localScope.closeDataPlaceholder();
  }

  public void addAllMixins(List<FullMixinDefinition> mixins) {
    localScope.addAllMixins(mixins);
  }

  public void add(IScope otherSope) {
    localScope.add(otherSope);
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

  @Override
  public ILocalScope cloneCurrentDataSnapshot() {
    return localScope.cloneCurrentDataSnapshot();
  }

  public LocalScopeData getLocalData() {
    return localScope.getLocalData();
  }

  public void createCurrentDataSnapshot() {
    localScope.createCurrentDataSnapshot();
  }

  public void createOriginalDataSnapshot() {
    localScope.createOriginalDataSnapshot();
  }

  public void discardLastDataSnapshot() {
    localScope.discardLastDataSnapshot();
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

  public void removedFromAst() {
    localScope.removedFromAst();
  }

  public boolean isPresentInAst() {
    return localScope.isPresentInAst();
  }

  public IScope getParent() {
    return surroundingScopes.getParent();
  }

  public void setParent(IScope parent) {
    surroundingScopes.setParent(parent);
  }

  public boolean hasParent() {
    return getParent()!=null; 
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

  public IScopesTree getSurroundingScopes() {
    return surroundingScopes;
  }

  public ILocalScope getLocalScope() {
    return localScope;
  }

}
