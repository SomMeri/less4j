package com.github.sommeri.less4j.core.compiler.scopes.local;

import java.util.Map.Entry;
import java.util.Set;

import com.github.sommeri.less4j.core.ast.AbstractVariableDeclaration;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionFilter;
import com.github.sommeri.less4j.core.compiler.scopes.local.KeyValueStorage.ValuePlaceholder;

public class VariablesDeclarationsStorage implements Cloneable {

  private KeyValueStorage<String, Expression> coolStorage = new KeyValueStorage<String, Expression>();

  public VariablesDeclarationsStorage() {
  }

  public Expression getValue(String name) {
    return coolStorage.getValue(name);
  }

  public void remove(String name) {
    coolStorage.remove(name);
  }

  public void store(AbstractVariableDeclaration node) {
    store(node.getVariable().getName(), node.getValue());
  }

  public void storeAll(VariablesDeclarationsStorage otherStorage) {
    coolStorage.add(otherStorage.coolStorage);
  }

  public void store(AbstractVariableDeclaration node, Expression replacementValue) {
    store(node.getVariable().getName(), replacementValue);
  }

  public void store(String name, Expression replacementValue) {
    coolStorage.add(name, replacementValue);
  }

  public void storeIfNotPresent(String name, Expression replacementValue) {
    if (!contains(name))
      store(name, replacementValue);
  }

  public void closePlaceholder() {
    coolStorage.closeFirstPlaceholder();
  }

  public void addFilteredVariables(ExpressionFilter filter, VariablesDeclarationsStorage variablesSource) {
    for (Entry<String, Expression> entry : variablesSource.coolStorage.getAllEntries()) {
      String name = entry.getKey();
      Expression value = entry.getValue();
      if (filter.accepts(name, value))
        store(name, filter.apply(value));
    }
  }

  protected boolean contains(String name) {
    return coolStorage.contains(name);
  }

  public int size() {
    return coolStorage.size();
  }

  public VariablesPlaceholder createPlaceholder() {
    return new VariablesPlaceholder(coolStorage.createPlaceholder());
  }

  public void addToFirstPlaceholderIfNotPresent(VariablesDeclarationsStorage otherStorage) {
    Set<Entry<String, Expression>> otherVariables = otherStorage.coolStorage.getAllEntries();
    for (Entry<String, Expression> entry : otherVariables) {
      if (!contains(entry.getKey()))
        coolStorage.addToFirstPlaceholder(entry.getKey(), entry.getValue());
    }
  }

  public void replacePlaceholder(VariablesPlaceholder placeholder, VariablesDeclarationsStorage otherStorage) {
    coolStorage.replacePlaceholder(placeholder.coolPlaceholder, otherStorage.coolStorage);
  }

  public VariablesDeclarationsStorage clone() {
    try {
      VariablesDeclarationsStorage clone = (VariablesDeclarationsStorage) super.clone();
      clone.coolStorage = coolStorage.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new IllegalStateException("Impossible state.");
    }
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder(getClass().getSimpleName()).append("\n");
    result.append("Variables: ").append(coolStorage);
    return result.toString();
  }

  public static class VariablesPlaceholder {

    private final ValuePlaceholder<String, Expression> coolPlaceholder;

    public VariablesPlaceholder(ValuePlaceholder<String, Expression> coolPlaceholder) {
      this.coolPlaceholder = coolPlaceholder;
    }

  }

}