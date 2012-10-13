package com.github.sommeri.less4j.core.compiler;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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

  public boolean hasVariable(String name) {
    return variables.containsKey(name);
  }

  public void removeDeclaration(String name) {
    variables.remove(name);
  }

  @Override
  protected VariablesScope clone() {
    VariablesScope result;
    try {
      result = (VariablesScope) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("This should never happen.", e);
    }
    result.variables = deeplyClonedMap(variables);
    return result;
  }

  private Map<String, Expression> deeplyClonedMap(Map<String, Expression> map) {
    Map<String, Expression> result = new HashMap<String, Expression>();
    for (Entry<String, Expression> t : map.entrySet()) {
      result.put(t.getKey(), t.getValue());
    }
    return result;
  }

}