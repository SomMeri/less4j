package com.github.sommeri.less4j.core.compiler.scopes.local;

import java.util.Map.Entry;
import java.util.Set;

import com.github.sommeri.less4j.core.ast.AbstractVariableDeclaration;
import com.github.sommeri.less4j.core.compiler.expressions.LocalScopeFilter;
import com.github.sommeri.less4j.core.compiler.scopes.FullNodeDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.local.KeyValueStorage.ValuePlaceholder;

public class VariablesDeclarationsStorage implements Cloneable {

  private KeyValueStorage<String, FullNodeDefinition> coolStorage = new KeyValueStorage<String, FullNodeDefinition>();

  public VariablesDeclarationsStorage() {
  }

  public FullNodeDefinition getValue(String name) {
    return coolStorage.getValue(name);
  }

  public void store(AbstractVariableDeclaration node) {
    store(node.getVariable().getName(), new FullNodeDefinition(node.getValue(), null));
  }

  public void storeAll(VariablesDeclarationsStorage otherStorage) {
    coolStorage.add(otherStorage.coolStorage);
  }

  public void store(AbstractVariableDeclaration node, FullNodeDefinition replacementValue) {
    store(node.getVariable().getName(), replacementValue);
  }

  public void store(String name, FullNodeDefinition replacementValue) {
    coolStorage.add(name, replacementValue);
  }

  public void storeIfNotPresent(String name, FullNodeDefinition replacementValue) {
    if (!contains(name))
      store(name, replacementValue);
  }

  public void closePlaceholder() {
    coolStorage.closeFirstPlaceholder();
  }

  //FIXME !!!!!!!!!!!!!!!!!! enable and fix
  public void addFilteredVariables(LocalScopeFilter filter, VariablesDeclarationsStorage source) {
    for (Entry<String, FullNodeDefinition> entry : source.coolStorage.getAllEntries()) {
      String name = entry.getKey();
      FullNodeDefinition value = entry.getValue();
      FullNodeDefinition filteredValue = filter.apply(value);
      
      store(name, filteredValue);
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
    Set<Entry<String, FullNodeDefinition>> otherVariables = otherStorage.coolStorage.getAllEntries();
    for (Entry<String, FullNodeDefinition> entry : otherVariables) {
      if (!contains(entry.getKey())) {
        coolStorage.addToFirstPlaceholder(entry.getKey(), entry.getValue());
      }
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

    private final ValuePlaceholder<String, FullNodeDefinition> coolPlaceholder;

    public VariablesPlaceholder(ValuePlaceholder<String, FullNodeDefinition> coolPlaceholder) {
      this.coolPlaceholder = coolPlaceholder;
    }

  }

}