package com.github.sommeri.less4j.core.compiler.remove;

import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.AbstractVariableDeclaration;
import com.github.sommeri.less4j.core.ast.ArgumentDeclaration;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.NamespaceReference;
import com.github.sommeri.less4j.core.ast.PureMixin;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.compiler.CompileException;
import com.github.sommeri.less4j.core.compiler.MixinsReferenceMatcher;
import com.github.sommeri.less4j.core.compiler.scopes.FullMixinDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.VariablesScope;

/**
 * Not exactly memory effective, but lets create working version first.  
 *
 */
@Deprecated
public class ActiveScope {

  private Stack<VariablesScope> variablesScope = new Stack<VariablesScope>();
  private Stack<MixinsScopeOld> MixinsScopeOld = new Stack<MixinsScopeOld>();
  private Stack<NamespaceTree> namespaces = new Stack<NamespaceTree>();

  public ActiveScope() {
    variablesScope.push(new VariablesScope());
    MixinsScopeOld.push(new MixinsScopeOld());
    namespaces.push(new NamespaceTree("#default#"));
  }

  public void addDeclaration(AbstractVariableDeclaration node) {
    variablesScope.peek().addDeclaration(node);
  }

  public void addDeclaration(AbstractVariableDeclaration node, Expression replacementValue) {
    variablesScope.peek().addDeclaration(node, replacementValue);
  }

  public void addDeclaration(Map<String, Expression> variablesState, ArgumentDeclaration node, Expression replacementValue) {
    variablesState.put(node.getVariable().getName(), replacementValue);
  }

  public void decreaseScope() {
    variablesScope.pop();
    MixinsScopeOld.pop();
  }

  public void increaseScope() {
    VariablesScope oldVariables = variablesScope.peek();
    variablesScope.push(new VariablesScope(oldVariables));
    MixinsScopeOld.push(new MixinsScopeOld());
    //TODO explain this line 
    if (!getCurrentNamespace().hasScope()) {
      getCurrentNamespace().setScope(MixinsScopeOld.peek());
    }
  }

  private NamespaceTree getCurrentNamespace() {
    return namespaces.peek();
  }

  /**
   * Returns null on unknown variable.
   * 
   * @param node
   * @return
   */
  public Expression getDeclaredValue(Variable node) {
    String name = node.getName();
    Expression expression = variablesScope.peek().getValue(name);
    if (expression == null)
      CompileException.throwUndeclaredVariable(node);

    return expression;
  }

  public Expression getDeclaredValue(String name, ASTCssNode ifErrorNode) {
    Expression expression = variablesScope.peek().getValue(name);
    if (expression == null)
      CompileException.throwUndeclaredVariable(name, ifErrorNode);

    return expression;
  }

  public List<FullMixinDefinitionOld> getAllMatchingMixins(MixinsReferenceMatcherOld matcher, MixinReference reference) {
    int idx = MixinsScopeOld.size();
    while (idx > 0) {
      idx--;
      MixinsScopeOld idxScope = MixinsScopeOld.elementAt(idx);
      if (idxScope.contains(reference.getName()))
        return matcher.filter(reference, idxScope.getMixins(reference.getName()));
    }

    throw CompileException.createUndeclaredMixin(reference);
  }

  public List<FullMixinDefinitionOld> getMixinsWithinNamespace(MixinsReferenceMatcherOld matcher, NamespaceReference reference) {
    //TODO what if noting is found NullPointer
    String mixin = reference.getFinalReference().getName();
    MixinsScopeOld scope = getCurrentNamespace().getMixinsScope();
    if (scope.contains(mixin))
      return matcher.filter(reference.getFinalReference(), scope.getMixins(mixin));

    return null;
  }

  //TODO: document namespace matching is not really well defined
  //FIXME: this should look in parents if unsuccessful and should be able to find multiple namespaces 
  public List<NamespaceTree> findReferencedNamespace(NamespaceReference reference) {
    List<String> nameChain = reference.getNameChain();
    NamespaceTree space = getCurrentNamespace();
    List<NamespaceTree> result = space.findMatchingChilds(nameChain);
    while (result.isEmpty()) {
      space = space.getParent();
      result = space.findMatchingChilds(nameChain);
    }
    return result;
  }

  public void removeVariablesOverride() {
    variablesScope.pop();
  }

  public void removeMixinsOverride() {
    MixinsScopeOld.pop();
  }

  public void registerMixin(PureMixin node) {
    VariablesScope variablesState = variablesScope.peek().clone();
    FullMixinDefinitionOld mixin = new FullMixinDefinitionOld(node, variablesState);
    MixinsScopeOld.peek().registerMixin(mixin);
  }

  public void overrideScopes(VariablesScope variables, MixinsScopeOld mixins) {
    overrideScope(variables);
    overrideScope(mixins);
  }

  private void overrideScope(MixinsScopeOld mixins) {
    MixinsScopeOld mixinsPeek = MixinsScopeOld.peek();
    mixins = mixins == null ? mixinsPeek : mixins;
    MixinsScopeOld.push(new MixinsScopeOld(mixinsPeek, mixins));
  }

  public void overrideScope(VariablesScope variables) {
    VariablesScope variablesPeek = variablesScope.peek();
    variablesScope.push(new VariablesScope(variablesPeek, variables));
  }

  //TODO: document - namespaces are valid only after being declared
  //TODO: pozor na nazvy metod, je v tom slusny bordel
  public void openNamespace(String name) {
    NamespaceTree space = new NamespaceTree(name, getCurrentNamespace());
    namespaces.push(space);
  }

  public void enterNamespace(NamespaceTree namespace) {
    namespaces.push(namespace);
    overrideScope(namespace.getMixinsScope());
  }

  public void leaveNamespace() {
    namespaces.pop();
    removeMixinsOverride();
  }

  public void closeNamespace() {
    namespaces.pop();
  }

}
