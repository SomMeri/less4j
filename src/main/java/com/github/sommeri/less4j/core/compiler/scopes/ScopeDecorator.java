package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.AbstractVariableDeclaration;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.ReusableStructureName;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionFilter;
import com.github.sommeri.less4j.core.compiler.scopes.refactoring.BasicScope;
import com.github.sommeri.less4j.core.compiler.scopes.refactoring.ILocalScope;
import com.github.sommeri.less4j.core.compiler.scopes.refactoring.SurroundingScopes;

public class ScopeDecorator extends BasicScope {

  private static final String DECORATOR = "#decorator#";
  private final IScope decoree;  
  
  //FIXME: (!!!) do naming correctly
  protected ScopeDecorator(IScope decoree) {
    //super(DECORATOR, null, decoree.toFullName());
    super(decoree, decoree.getSurroundingScopes());
    this.decoree = decoree;
  }

  public void addNames(List<String> names) {
    decoree.addNames(names);
  }

  public List<IScope> getChilds() {
    return decoree.getChilds();
  }

  public List<String> getNames() {
    return decoree.getNames();
  }

  public void addFilteredVariables(ExpressionFilter filter, IScope source) {
    decoree.addFilteredVariables(filter, source);
  }

  public void addAllMixins(List<FullMixinDefinition> mixins) {
    decoree.addAllMixins(mixins);
  }

  public void add(IScope otherSope) {
    decoree.add(otherSope);
  }

  public void addVariables(IScope otherSope) {
    decoree.addVariables(otherSope);
  }

  public void createPlaceholder() {
    decoree.createPlaceholder();
  }

  public void addToPlaceholder(IScope otherScope) {
    decoree.addToPlaceholder(otherScope);
  }

  public void closePlaceholder() {
    decoree.closePlaceholder();
  }

  public List<FullMixinDefinition> getAllMixins() {
    return decoree.getAllMixins();
  }

  public List<FullMixinDefinition> getMixinsByName(List<String> nameChain, ReusableStructureName name) {
    return decoree.getMixinsByName(nameChain, name);
  }

  public List<FullMixinDefinition> getMixinsByName(ReusableStructureName name) {
    return decoree.getMixinsByName(name);
  }

  public List<FullMixinDefinition> getMixinsByName(String name) {
    return decoree.getMixinsByName(name);
  }

  public IScope firstChild() {
    return decoree.firstChild();
  }

  public IScope getChildOwnerOf(ASTCssNode body) {
    return decoree.getChildOwnerOf(body);
  }

  public IScope childByOwners(ASTCssNode headNode, ASTCssNode... restNodes) {
    return decoree.childByOwners(headNode, restNodes);
  }

  public boolean equals(Object obj) {
    return decoree.equals(obj);
  }

  public IScope getParent() {
    return decoree.getParent();
  }

  public boolean hasParent() {
    return decoree.hasParent();
  }

  public String toFullName() {
    return decoree.toFullName();
  }

  public void registerVariable(AbstractVariableDeclaration declaration) {
    decoree.registerVariable(declaration);
  }

  public void registerVariable(AbstractVariableDeclaration node, Expression replacementValue) {
    decoree.registerVariable(node, replacementValue);
  }

  public void registerVariableIfNotPresent(String name, Expression replacementValue) {
    decoree.registerVariableIfNotPresent(name, replacementValue);
  }

  public void registerVariable(String name, Expression replacementValue) {
    decoree.registerVariable(name, replacementValue);
  }

  public Expression getValue(Variable variable) {
    return decoree.getValue(variable);
  }

  public Expression getValue(String name) {
    return decoree.getValue(name);
  }

  public Expression getLocalValue(Variable variable) {
    return decoree.getLocalValue(variable);
  }

  public Expression getLocalValue(String name) {
    return decoree.getLocalValue(name);
  }

  public void registerMixin(ReusableStructure mixin, IScope mixinsBodyScope) {
    decoree.registerMixin(mixin, mixinsBodyScope);
  }

  public boolean isBodyOwnerScope() {
    return decoree.isBodyOwnerScope();
  }

  public IScope skipBodyOwner() {
    return decoree.skipBodyOwner();
  }

  public String toLongString() {
    return decoree.toLongString();
  }

  public void setParent(IScope parent) {
    //FIXME: (!!!) following check is needed due to constructor call in Scope - needs clean up after we know things really got faster
    if (decoree!=null)
      decoree.setParent(parent);
  }

  public void removedFromAst() {
    decoree.removedFromAst();
  }

  public boolean isPresentInAst() {
    return decoree.isPresentInAst();
  }

  public IScope getRootScope() {
    return decoree.getRootScope();
  }

  public boolean seesLocalDataOf(IScope otherScope) {
    return decoree.seesLocalDataOf(otherScope);
  }

  public int getTreeSize() {
    return decoree.getTreeSize();
  }

  public int hashCode() {
    return decoree.hashCode();
  }

  @Override
  public boolean hasTheSameLocalData(ILocalScope otherScope) {
    return decoree.getLocalScope().hasTheSameLocalData(otherScope);
  }

  @Override
  public void createLocalDataSnapshot() {
    decoree.createLocalDataSnapshot();
  }

  @Override
  public void discardLastLocalDataSnapshot() {
    decoree.discardLastLocalDataSnapshot();
  }

  @Override
  public VariablesDeclarationsStorage getLocalVariables() {
    return decoree.getLocalVariables();
  }

  @Override
  public LocalScopeData getLocalData() {
    return decoree.getLocalData();
  }
  
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder(toFullName());
    result.append("\n\n").append(decoree.toString());
    return result.toString();
  }

}
