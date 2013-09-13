package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.List;
import java.util.Stack;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.AbstractVariableDeclaration;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.ReusableStructureName;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionFilter;
import com.github.sommeri.less4j.core.compiler.scopes.refactoring.ILocalScope;
import com.github.sommeri.less4j.core.compiler.scopes.refactoring.ScopeFactory;

public class LocalScope implements ILocalScope {

  // following is used only during debugging - to generate human readable toString
  private String type;
  private final ASTCssNode owner;
  private boolean presentInAst = true;
  
  private LocalScopeData localData = new LocalScopeData();
  private Stack<LocalScopeData> localDataSnapshots = new Stack<LocalScopeData>();

  private List<String> names;

  public LocalScope(ASTCssNode owner, List<String> names, String type) {
    this.owner = owner;
    this.names = names;
    this.type = type;
  }

  public LocalScope(ASTCssNode owner, LocalScopeData initialLocalData, List<String> names, String type) {
    this(owner, names, type);
    localData =  initialLocalData;
  }

  @Override
  public ASTCssNode getOwner() {
    return owner;
  }

  public String getType() {
    return type;
  }

  public boolean isBodyOwnerScope() {
    return ScopeFactory.BODY_OWNER.equals(getType());
  }

  public void removedFromAst() {
    presentInAst = false;
  }

  public boolean isPresentInAst() {
    return presentInAst;
  }

  public void addNames(List<String> names) {
    this.names.addAll(names);
  }

  public List<String> getNames() {
    return names;
  }

  public void registerVariable(AbstractVariableDeclaration declaration) {
    getLocalVariables().store(declaration);
  }

  public void registerVariable(AbstractVariableDeclaration node, Expression replacementValue) {
    getLocalVariables().store(node, replacementValue);
  }

  public void registerVariableIfNotPresent(String name, Expression replacementValue) {
    getLocalVariables().storeIfNotPresent(name, replacementValue);
  }

  public void registerVariable(String name, Expression replacementValue) {
    getLocalVariables().store(name, replacementValue);
  }

  public void addFilteredVariables(ExpressionFilter filter, IScope source) {
    getLocalVariables().addFilteredVariables(filter, source.getLocalVariables());
  }

  //FIXME: (!!!) ugly
  @Deprecated
  public void addVariables(IScope otherSope) {
    getLocalVariables().storeAll(otherSope.getLocalVariables());
  }

  public Expression getValue(Variable variable) {
    return getLocalVariables().getValue(variable.getName());
  }

  public Expression getValue(String name) {
    return getLocalVariables().getValue(name);
  }

  @Override
  public void addAllMixins(List<FullMixinDefinition> mixins) {
    getLocalMixins().storeAll(mixins);
  }

  public void registerMixin(ReusableStructure mixin, IScope mixinsBodyScope) {
    getLocalMixins().store(new FullMixinDefinition(mixin, mixinsBodyScope));
  }

  public void createPlaceholder() {
    getLocalVariables().createPlaceholder();
    getLocalMixins().createPlaceholder();
  }

  public void addToPlaceholder(IScope otherScope) {
    getLocalVariables().addToPlaceholder(otherScope.getLocalVariables());
    getLocalMixins().addToPlaceholder(otherScope.getLocalMixins());
  }

  public void closePlaceholder() {
    getLocalVariables().closePlaceholder();
    getLocalMixins().closePlaceholder();
  }

  public void add(IScope otherSope) {
    getLocalMixins().storeAll(otherSope.getLocalMixins());
    getLocalVariables().storeAll(otherSope.getLocalVariables());
  }

  public LocalScopeData getLocalData() {
    return localData;
  }

  public boolean hasTheSameLocalData(ILocalScope otherScope) {
    return otherScope.getLocalData() == localData;
  }

  /**
   * Do not call this method directly. Use {@link InScopeSnapshotRunner}
   * instead.
   */
  public void createLocalDataSnapshot() {
    localDataSnapshots.push(localData);
    localData = localData.clone();
  }

  /**
   * Do not call this method directly. Use {@link InScopeSnapshotRunner}
   * instead.
   */
  public void discardLastLocalDataSnapshot() {
    localData = localDataSnapshots.pop();
  }

  public MixinsDefinitionsStorage getLocalMixins() {
    return localData.getMixins();
  }

  public VariablesDeclarationsStorage getLocalVariables() {
    return localData.getVariables();
  }

  public List<FullMixinDefinition> getAllMixins() {
    return getLocalMixins().getAllMixins();
  }

  public List<FullMixinDefinition> getMixinsByName(List<String> nameChain, ReusableStructureName name) {
    return getLocalMixins().getMixins(nameChain, name);
  }

  public List<FullMixinDefinition> getMixinsByName(ReusableStructureName name) {
    return getLocalMixins().getMixins(name);
  }

  public List<FullMixinDefinition> getMixinsByName(String name) {
    return getLocalMixins().getMixins(name);
  }

  @Override
  public String toString() {
    return localData.toString();
  }

}
