package com.github.sommeri.less4j.core.compiler;

import java.util.HashMap;
import java.util.Map;

import com.github.sommeri.less4j.core.ast.AbstractVariableDeclaration;
import com.github.sommeri.less4j.core.ast.Expression;

public class VariablesScope implements Cloneable {

  private Map<String, Expression> variables = new HashMap<String, Expression>();

  public VariablesScope(VariablesScope scope) {
    variables = new HashMap<String, Expression>(scope.variables);
  }

  public VariablesScope() {
  }

  public Expression getValue(String name) {
    return variables.get(name);
  }

  public void addDeclaration(AbstractVariableDeclaration node) {
    variables.put(node.getVariable().getName(), node.getValue());
  }

  public void addDeclaration(AbstractVariableDeclaration node, Expression replacementValue) {
    variables.put(node.getVariable().getName(), replacementValue);
  }

  public void addDeclaration(String name, Expression replacementValue) {
    variables.put(name, replacementValue);
  }

  public void addDeclarationIfNotPresent(String name, Expression replacementValue) {
    if (!variables.containsKey(name))
      variables.put(name, replacementValue);
  }

  public void removeDeclaration(String name) {
    variables.remove(name);
  }

  @Override
  protected VariablesScope clone() {
    try {
      VariablesScope result = (VariablesScope) super.clone();
      result.variables = new HashMap<String, Expression>(variables);
      return result;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("This should never happen.", e);
    }
  }

}