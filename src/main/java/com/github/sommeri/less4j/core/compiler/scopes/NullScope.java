package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.AbstractVariableDeclaration;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.ReusableStructureName;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionFilter;
import com.github.sommeri.less4j.core.compiler.scopes.local.LocalScope;

public class NullScope extends BasicScope {

  private static final String NULL = "#null#";

  public NullScope() {
    super(new LocalScope(null, Arrays.asList(NULL), NULL), new ScopesTree());
  }

  @Override
  public IScope getParent() {
    return this;
  }

  @Override
  public List<IScope> getChilds() {
    return Collections.emptyList();
  }

  @Override
  public List<String> getNames() {
    return Collections.emptyList();
  }

  @Override
  public boolean hasParent() {
    return false;
  }

  @Override
  public void registerVariable(AbstractVariableDeclaration declaration) {
  }

  @Override
  public void registerVariable(AbstractVariableDeclaration node, Expression replacementValue) {
  }

  @Override
  public void registerVariableIfNotPresent(String name, Expression replacementValue) {
  }

  @Override
  public Expression getValue(Variable variable) {
    return null;
  }

  @Override
  public Expression getValue(String name) {
    return null;
  }

  @Override
  public void registerMixin(ReusableStructure mixin, IScope mixinsBodyScope) {
  }

  //  @Override
  //  public void setParent(IScope parent) {
  //  }

  @Override
  public void removedFromAst() {
  }

  @Override
  public boolean isPresentInAst() {
    return false;
  }

  @Override
  public IScope getRootScope() {
    return this;
  }

  @Override
  public IScope getChildOwnerOf(ASTCssNode body) {
    return null;
  }

  @Override
  public void addNames(List<String> names) {
  }

  @Override
  public String toFullName() {
    return toLongString();
  }

  @Override
  public void registerVariable(String name, Expression replacementValue) {
  }

  @Override
  public void addFilteredVariables(ExpressionFilter filter, IScope source) {
  }

  @Override
  public void addAllMixins(List<FullMixinDefinition> mixins) {
  }

  @Override
  public void add(IScope otherSope) {
  }

  @Override
  public DataPlaceholder createDataPlaceholder() {
    return null;
  }

  @Override
  public void addToDataPlaceholder(IScope otherScope) {
  }

  @Override
  public void closeDataPlaceholder() {
  }

  @Override
  public List<FullMixinDefinition> getAllMixins() {
    return Collections.emptyList();
  }

  @Override
  public List<FullMixinDefinition> getMixinsByName(List<String> nameChain, ReusableStructureName name) {
    return Collections.emptyList();
  }

  @Override
  public List<FullMixinDefinition> getMixinsByName(ReusableStructureName name) {
    return Collections.emptyList();
  }

  @Override
  public List<FullMixinDefinition> getMixinsByName(String name) {
    return Collections.emptyList();
  }

  @Override
  public IScope firstChild() {
    return null;
  }

  @Override
  public boolean isBodyOwnerScope() {
    return false;
  }

  @Override
  public IScope skipBodyOwner() {
    return this;
  }

  @Override
  public String toLongString() {
    return NULL;
  }

  @Override
  public boolean seesLocalDataOf(IScope otherScope) {
    return false;
  }

  @Override
  public IScope childByOwners(ASTCssNode headNode, ASTCssNode... restNodes) {
    return this;
  }

  @Override
  public int getTreeSize() {
    return 1;
  }

}
