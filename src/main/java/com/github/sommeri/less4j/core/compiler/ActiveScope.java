package com.github.sommeri.less4j.core.compiler;

import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.AbstractVariableDeclaration;
import com.github.sommeri.less4j.core.ast.ArgumentDeclaration;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.PureMixin;
import com.github.sommeri.less4j.core.ast.Variable;

/**
 * Not exactly memory effective, but lets create working version first.  
 *
 */
public class ActiveScope {

  private Stack<VariablesScope> variablesScope = new Stack<VariablesScope>();
  private Stack<MixinsScope> mixinsScope = new Stack<MixinsScope>();
  private Stack<String> mixinsStack = new Stack<String>();

  public ActiveScope() {
    variablesScope.push(new VariablesScope());
    mixinsScope.push(new MixinsScope());
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

  public void enteringPureMixin(PureMixin node) {
    mixinsStack.push(node.getName());
  }

  public boolean isInPureMixin() {
    return !mixinsStack.isEmpty();
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

  public void leaveMixinVariableScope() {
    variablesScope.pop();
  }

  public void leavingPureMixin(PureMixin node) {
    mixinsStack.pop();
  }

  public void registerMixin(PureMixin node) {
    VariablesScope variablesState = variablesScope.peek().clone();
    FullMixinDefinition mixin = new FullMixinDefinition(node, variablesState);
    mixinsScope.peek().registerMixin(mixin);
  }

  public void enterMixinVariableScope(VariablesScope variablesUponDefinition) {
    variablesScope.push(variablesUponDefinition);
  }
}
