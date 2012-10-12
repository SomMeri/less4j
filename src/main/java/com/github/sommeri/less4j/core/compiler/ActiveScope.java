package com.github.sommeri.less4j.core.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

  private Stack<Map<String, Expression>> variablesScope = new Stack<Map<String, Expression>>();
  //mixins catalogues - stores each encountered mixin upon entry and
  private Stack<Map<String, List<MixinWithScope>>> mixinsScope = new Stack<Map<String, List<MixinWithScope>>>();
  private Stack<String> mixinsStack = new Stack<String>();

  public ActiveScope() {
    variablesScope.push(new HashMap<String, Expression>());
    mixinsScope.push(new HashMap<String, List<MixinWithScope>>());
  }

  public void addDeclaration(AbstractVariableDeclaration node) {
    addDeclaration(variablesScope.peek(), node);
  }

  public void addDeclaration(Map<String, Expression> variablesState, AbstractVariableDeclaration node) {
    variablesState.put(node.getVariable().getName(), node.getValue());
  }

  public void addDeclaration(AbstractVariableDeclaration node, Expression replacementValue) {
    variablesScope.peek().put(node.getVariable().getName(), replacementValue);
  }

  public void addDeclaration(Map<String, Expression> variablesState, ArgumentDeclaration node, Expression replacementValue) {
    variablesState.put(node.getVariable().getName(), replacementValue);
  }

  public void decreaseScope() {
    variablesScope.pop();
    mixinsScope.pop();
  }

  public void increaseScope() {
    Map<String, Expression> oldVariables = variablesScope.peek();
    variablesScope.push(new HashMap<String, Expression>(oldVariables));
    HashMap<String, List<MixinWithScope>> newMixins = new HashMap<String, List<MixinWithScope>>();
    mixinsScope.push(new HashMap<String, List<MixinWithScope>>(newMixins));
  }

  public Expression getDeclaredValue(Variable node) {
    String name = node.getName();
    Expression expression = variablesScope.peek().get(name);
    if (expression == null)
      CompileException.throwUndeclaredVariable(node);

    return expression;
  }

  public Expression getDeclaredValue(String name, ASTCssNode ifErrorNode) {
    Expression expression = variablesScope.peek().get(name);
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

  public List<MixinWithScope> getAllMatchingMixins(MixinReference reference) {
    int idx = mixinsScope.size();
    while (idx > 0) {
      idx--;
      Map<String, List<MixinWithScope>> idxScope = mixinsScope.elementAt(idx);
      if (idxScope.containsKey(reference.getName()))
        return filterByParametersNumber(reference, idxScope.get(reference.getName()));
    }
    throw CompileException.createUndeclaredMixin(reference);
  }

  private List<MixinWithScope> filterByParametersNumber(MixinReference reference, List<MixinWithScope> list) {
    int requiredNumber = reference.getParameters().size();
    List<MixinWithScope> result = new ArrayList<MixinWithScope>();
    for (MixinWithScope mixinWithScope : list) {
      int allDefined = mixinWithScope.getMixin().getParameters().size();
      int mandatory = mixinWithScope.getMixin().getMandatoryParameters().size();
      if (requiredNumber>=mandatory && requiredNumber<=allDefined)
        result.add(mixinWithScope);
    }
    return result;
  }

  public void leaveMixinVariableScope() {
    variablesScope.pop();
  }
  
  private Map<String, Expression> deeplyClonedMap(Map<String, Expression> map) {
    Map<String, Expression> result = new HashMap<String, Expression>();
    for (Entry<String, Expression> t : map.entrySet()) {
      result.put(t.getKey(), t.getValue());
    }
    return result;
  }

  public void leavingPureMixin(PureMixin node) {
    mixinsStack.pop();
  }

  public void registerMixin(PureMixin node) {
    Map<String, Expression> variablesState = deeplyClonedMap(variablesScope.peek());
    Map<String, List<MixinWithScope>> catalogue = mixinsScope.peek();
    List<MixinWithScope> list = catalogue.get(node.getName());
    if (list==null) {
      list=new ArrayList<MixinWithScope>();
      catalogue.put(node.getName(), list);
    }
    list.add(new MixinWithScope(node, variablesState));
  }

  public void enterMixinVariableScope(Map<String, Expression> variablesUponDefinition) {
    variablesScope.push(variablesUponDefinition);
  }

}

