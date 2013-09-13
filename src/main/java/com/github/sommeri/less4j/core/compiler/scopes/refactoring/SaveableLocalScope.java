package com.github.sommeri.less4j.core.compiler.scopes.refactoring;

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

public class SaveableLocalScope implements ILocalScope {
  
  private final ILocalScope originalLocalScope;
  private LocalScopeData savedLocalData;

  public SaveableLocalScope(ILocalScope originalLocalScope) {
    super();
    this.originalLocalScope = originalLocalScope;
  }

  public void save() {
    this.savedLocalData = getLocalData().clone();
  }
  
  public Expression getValue(Variable variable) {
    if (savedLocalData!=null) {
      //FIXME: (!!!) wrong and weird, probably incorrect (unsnapshoted version will be searched)
      Expression value = savedLocalData.getVariables().getValue(variable.getName());
      if (value!=null)
        return value;
    }
    return originalLocalScope.getValue(variable);
  }

  public Expression getValue(String name) {
    if (savedLocalData!=null) {
      //FIXME: (!!!) wrong and weird, probably incorrect (unsnapshoted version will be searched)
      Expression value = savedLocalData.getVariables().getValue(name);
      if (value!=null)
        return value;
    }
    return originalLocalScope.getValue(name);
  }

  public ASTCssNode getOwner() {
    return originalLocalScope.getOwner();
  }

  public String getType() {
    return originalLocalScope.getType();
  }

  public boolean isBodyOwnerScope() {
    return originalLocalScope.isBodyOwnerScope();
  }

  public void removedFromAst() {
    originalLocalScope.removedFromAst();
  }

  public boolean isPresentInAst() {
    return originalLocalScope.isPresentInAst();
  }

  public void addNames(List<String> names) {
    originalLocalScope.addNames(names);
  }

  public List<String> getNames() {
    return originalLocalScope.getNames();
  }

  public void registerVariable(AbstractVariableDeclaration declaration) {
    originalLocalScope.registerVariable(declaration);
  }

  public void registerVariable(AbstractVariableDeclaration node, Expression replacementValue) {
    originalLocalScope.registerVariable(node, replacementValue);
  }

  public void registerVariableIfNotPresent(String name, Expression replacementValue) {
    originalLocalScope.registerVariableIfNotPresent(name, replacementValue);
  }

  public void registerVariable(String name, Expression replacementValue) {
    originalLocalScope.registerVariable(name, replacementValue);
  }

  public void addFilteredVariables(ExpressionFilter filter, IScope source) {
    originalLocalScope.addFilteredVariables(filter, source);
  }

  public void addVariables(IScope otherSope) {
    originalLocalScope.addVariables(otherSope);
  }

  public void addAllMixins(List<FullMixinDefinition> mixins) {
    originalLocalScope.addAllMixins(mixins);
  }

  public void registerMixin(ReusableStructure mixin, IScope mixinsBodyScope) {
    originalLocalScope.registerMixin(mixin, mixinsBodyScope);
  }

  public void createPlaceholder() {
    originalLocalScope.createPlaceholder();
  }

  public void addToPlaceholder(IScope otherScope) {
    originalLocalScope.addToPlaceholder(otherScope);
  }

  public void closePlaceholder() {
    originalLocalScope.closePlaceholder();
  }

  public void add(IScope otherSope) {
    originalLocalScope.add(otherSope);
  }

  public LocalScopeData getLocalData() {
    return originalLocalScope.getLocalData();
  }

  public boolean hasTheSameLocalData(ILocalScope otherScope) {
    return originalLocalScope.hasTheSameLocalData(otherScope);
  }

  public void createLocalDataSnapshot() {
    originalLocalScope.createLocalDataSnapshot();
  }

  public void discardLastLocalDataSnapshot() {
    originalLocalScope.discardLastLocalDataSnapshot();
  }

  public MixinsDefinitionsStorage getLocalMixins() {
    return originalLocalScope.getLocalMixins();
  }

  public VariablesDeclarationsStorage getLocalVariables() {
    return originalLocalScope.getLocalVariables();
  }

  public List<FullMixinDefinition> getAllMixins() {
    return originalLocalScope.getAllMixins();
  }

  public List<FullMixinDefinition> getMixinsByName(List<String> nameChain, ReusableStructureName name) {
    return originalLocalScope.getMixinsByName(nameChain, name);
  }

  public List<FullMixinDefinition> getMixinsByName(ReusableStructureName name) {
    return originalLocalScope.getMixinsByName(name);
  }

  public List<FullMixinDefinition> getMixinsByName(String name) {
    return originalLocalScope.getMixinsByName(name);
  }

  public String toString() {
    return originalLocalScope.toString();
  }

}
