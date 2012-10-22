package com.github.sommeri.less4j.core.compiler.scopes;

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

/**
 * Not exactly memory effective, but lets create working version first.  
 *
 */
public class ActiveScope {

  private Stack<VariablesScope> variablesScope = new Stack<VariablesScope>();
  private Stack<MixinsScope> mixinsScope = new Stack<MixinsScope>();
  private Stack<NamespaceTree> namespaces = new Stack<NamespaceTree>();

  public ActiveScope() {
    variablesScope.push(new VariablesScope());
    mixinsScope.push(new MixinsScope());
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
    mixinsScope.pop();
  }

  public void increaseScope() {
    VariablesScope oldVariables = variablesScope.peek();
    variablesScope.push(new VariablesScope(oldVariables));
    mixinsScope.push(new MixinsScope());
    //TODO explain this line 
    if (!getCurrentNamespace().hasScope()) {
      getCurrentNamespace().setScope(mixinsScope.peek());
    }
  }

  private NamespaceTree getCurrentNamespace() {
    return namespaces.peek();
  }

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

  public List<FullMixinDefinition> getAllMatchingMixins(MixinsReferenceMatcher matcher, MixinReference reference) {
    int idx = mixinsScope.size();
    while (idx > 0) {
      idx--;
      MixinsScope idxScope = mixinsScope.elementAt(idx);
      if (idxScope.contains(reference.getName()))
        return matcher.filter(reference, idxScope.getMixins(reference.getName()));
    }

    throw CompileException.createUndeclaredMixin(reference);
  }

  public List<FullMixinDefinition> getMixinsWithinNamespace(MixinsReferenceMatcher matcher, NamespaceReference reference) {
    //TODO what if noting is found NullPointer
    String mixin = reference.getFinalReference().getName();
    MixinsScope scope = getCurrentNamespace().getMixinsScope();
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
    mixinsScope.pop();
  }

  public void registerMixin(PureMixin node) {
    VariablesScope variablesState = variablesScope.peek().clone();
    FullMixinDefinition mixin = new FullMixinDefinition(node, variablesState);
    mixinsScope.peek().registerMixin(mixin);
  }

  public void overrideScopes(VariablesScope variables, MixinsScope mixins) {
    overrideScope(variables);
    overrideScope(mixins);
  }

  private void overrideScope(MixinsScope mixins) {
    MixinsScope mixinsPeek = mixinsScope.peek();
    mixins = mixins == null ? mixinsPeek : mixins;
    mixinsScope.push(new MixinsScope(mixinsPeek, mixins));
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
