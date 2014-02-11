package com.github.sommeri.less4j.core.compiler.scopes.local;

import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.AbstractVariableDeclaration;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.ReusableStructureName;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionFilter;
import com.github.sommeri.less4j.core.compiler.scopes.FullMixinDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.ILocalScope;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;

public class SaveableLocalScope implements ILocalScope {
  
  private final ILocalScope originalLocalScope;
  private ILocalScope savedData;

  public SaveableLocalScope(ILocalScope originalLocalScope) {
    super();
    this.originalLocalScope = originalLocalScope;
  }

  public void save() {
    this.savedData = getActiveLocalScope().cloneCurrentDataSnapshot();
  }

  private ILocalScope getActiveLocalScope() {
    if (savedData!=null)
      return savedData;
    
    return originalLocalScope;
  }
  
  public Expression getValue(Variable variable) {
    return getActiveLocalScope().getValue(variable);
  }

  public Expression getValue(String name) {
    return getActiveLocalScope().getValue(name);
  }

  public ASTCssNode getOwner() {
    return getActiveLocalScope().getOwner();
  }

  public String getType() {
    return getActiveLocalScope().getType();
  }

  public boolean isBodyOwnerScope() {
    return getActiveLocalScope().isBodyOwnerScope();
  }

  public void removedFromAst() {
    getActiveLocalScope().removedFromAst();
  }

  public boolean isPresentInAst() {
    return getActiveLocalScope().isPresentInAst();
  }

  public void addNames(List<String> names) {
    getActiveLocalScope().addNames(names);
  }

  public List<String> getNames() {
    return getActiveLocalScope().getNames();
  }

  public void registerVariable(AbstractVariableDeclaration declaration) {
    getActiveLocalScope().registerVariable(declaration);
  }

  public void registerVariable(AbstractVariableDeclaration node, Expression replacementValue) {
    getActiveLocalScope().registerVariable(node, replacementValue);
  }
  
  public void registerVariableIfNotPresent(String name, Expression replacementValue) {
    getActiveLocalScope().registerVariableIfNotPresent(name, replacementValue);
  }

  public void registerVariable(String name, Expression replacementValue) {
    getActiveLocalScope().registerVariable(name, replacementValue);
  }

  public void addFilteredVariables(ExpressionFilter filter, IScope source) {
    getActiveLocalScope().addFilteredVariables(filter, source);
  }

  public void addAllMixins(List<FullMixinDefinition> mixins) {
    getActiveLocalScope().addAllMixins(mixins);
  }

  public void registerMixin(ReusableStructure mixin, IScope mixinsBodyScope) {
    getActiveLocalScope().registerMixin(mixin, mixinsBodyScope);
  }

  public DataPlaceholder createDataPlaceholder() {
    return getActiveLocalScope().createDataPlaceholder();
  }

  public void addToDataPlaceholder(IScope otherScope) {
    getActiveLocalScope().addToDataPlaceholder(otherScope);
  }

  public void replacePlaceholder(DataPlaceholder placeholder, IScope otherScope) {
    getActiveLocalScope().replacePlaceholder(placeholder, otherScope);
  }

  public void closeDataPlaceholder() {
    getActiveLocalScope().closeDataPlaceholder();
  }

  public void add(IScope otherSope) {
    getActiveLocalScope().add(otherSope);
  }

  public LocalScopeData getLocalData() {
    return getActiveLocalScope().getLocalData();
  }

  public ILocalScope cloneCurrentDataSnapshot() {
    return getActiveLocalScope().cloneCurrentDataSnapshot();
  }

  public boolean hasTheSameLocalData(ILocalScope otherScope) {
    return getActiveLocalScope().hasTheSameLocalData(otherScope);
  }

  public void createCurrentDataSnapshot() {
    getActiveLocalScope().createCurrentDataSnapshot();
  }

  public void createOriginalDataSnapshot() {
    getActiveLocalScope().createOriginalDataSnapshot();
  }

  public void discardLastDataSnapshot() {
    getActiveLocalScope().discardLastDataSnapshot();
  }

  public MixinsDefinitionsStorage getLocalMixins() {
    return getActiveLocalScope().getLocalMixins();
  }

  public VariablesDeclarationsStorage getLocalVariables() {
    return getActiveLocalScope().getLocalVariables();
  }

  public List<FullMixinDefinition> getAllMixins() {
    return getActiveLocalScope().getAllMixins();
  }

  public List<FullMixinDefinition> getMixinsByName(List<String> nameChain, ReusableStructureName name) {
    return getActiveLocalScope().getMixinsByName(nameChain, name);
  }

  public List<FullMixinDefinition> getMixinsByName(ReusableStructureName name) {
    return getActiveLocalScope().getMixinsByName(name);
  }

  public List<FullMixinDefinition> getMixinsByName(String name) {
    return getActiveLocalScope().getMixinsByName(name);
  }

  public String toString() {
    return getActiveLocalScope().toString();
  }

}
