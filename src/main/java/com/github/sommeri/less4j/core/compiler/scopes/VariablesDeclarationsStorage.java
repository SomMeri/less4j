package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.sommeri.less4j.core.ast.AbstractVariableDeclaration;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionFilter;

public class VariablesDeclarationsStorage extends StorageWithPlaceholders<Expression> {

  private Map<String, Expression> variables = new HashMap<String, Expression>();

  public VariablesDeclarationsStorage() {
  }

  public Expression getValue(String name) {
    return variables.get(name);
  }

  public void store(AbstractVariableDeclaration node) {
    store(node.getVariable().getName(), node.getValue());
  }

  public void storeAll(VariablesDeclarationsStorage otherStorage) {
    for (Entry<String, Expression> entry : otherStorage.variables.entrySet()) {
      store(entry.getKey(), entry.getValue());
    }
  }

  public void store(AbstractVariableDeclaration node, Expression replacementValue) {
    store(node.getVariable().getName(), replacementValue);
  }

  @Override
  protected void doStore(String name, Expression replacementValue) {
    variables.put(name, replacementValue);
  }

  @Override
  protected void doStore(String name, List<Expression> value) {
    throw new IllegalStateException("not implemented method");
  }

  public void storeIfNotPresent(String name, Expression replacementValue) {
    if (!contains(name))
      store(name, replacementValue);
  }

  public void addFilteredVariables(ExpressionFilter filter, VariablesDeclarationsStorage variablesSource) {
    for (Entry<String, Expression> entry : variablesSource.variables.entrySet()) {
      String name = entry.getKey();
      Expression value = entry.getValue();
      store(name, filter.apply(value));
    }
  }

  protected boolean contains(String name) {
    return variables.containsKey(name);
  }

  public int size() {
    return variables.size();
  }

  public void addToPlaceholder(VariablesDeclarationsStorage otherStorage) {
    Map<String, Expression> otherVariables = otherStorage.variables;
    for (Entry<String, Expression> entry : otherVariables.entrySet()) {
      String name = entry.getKey();
      Expression value = entry.getValue();
      
      if (storedBeforeUnusedPlaceholder(name) && !contains(name))
        store(name, value);
    }
  }

  public VariablesDeclarationsStorage clone() {
    VariablesDeclarationsStorage clone = (VariablesDeclarationsStorage) super.clone();
    clone.variables = new HashMap<String, Expression>(variables);
    return clone;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder(getClass().getSimpleName()).append("\n");;
    result.append("Variables: ").append(variables);
    return result.toString();
  }
  
  
}