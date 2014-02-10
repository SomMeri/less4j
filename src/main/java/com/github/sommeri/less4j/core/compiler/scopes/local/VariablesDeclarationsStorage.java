package com.github.sommeri.less4j.core.compiler.scopes.local;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.sommeri.less4j.core.ast.AbstractVariableDeclaration;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionFilter;
import com.github.sommeri.less4j.utils.ArraysUtils;

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
    StoragePlaceholder<Expression> placeholder = getFirstUnusedPlaceholder();
    addToPlaceholder(placeholder, otherStorage, true);
  }

  private void addToPlaceholder(StoragePlaceholder<Expression> placeholder, VariablesDeclarationsStorage otherStorage, boolean localScopeProtection) {
    Map<String, Expression> otherVariables = otherStorage.variables;
    for (Entry<String, Expression> entry : otherVariables.entrySet()) {
      String name = entry.getKey();
      Expression value = entry.getValue();
      
      if (storedUnderPlaceholder(name, placeholder) && (!localScopeProtection || !contains(name))) //local scope protection
        store(name, value);
    }
  }

  public void replacePlaceholder(StoragePlaceholder<Expression> placeholder, VariablesDeclarationsStorage otherStorage) {
    // add variables if it not overwritten yet into that placeholder
    addToPlaceholder(placeholder,  otherStorage, false);
    // raise  placeholders ids if higher
    int placeholdersPosition = getPosition(placeholder);
    StoragePlaceholder<Expression> previousPlaceholder = ArraysUtils.last(otherStorage.getPlaceholders());
    if (previousPlaceholder==null && placeholdersPosition>0)
      previousPlaceholder = getPlaceholders().get(placeholdersPosition-1);
    getPlaceholders().addAll(placeholdersPosition, otherStorage.getPlaceholders());
    getPlaceholders().remove(placeholder);
    
    Map<String, StoragePlaceholder<Expression>> newPlaceholdersWhenModified = new HashMap<String, StoragePlaceholder<Expression>>();
    for (Entry<String, StoragePlaceholder<Expression>> entry : placeholdersWhenModified.entrySet()) {
      StoragePlaceholder<Expression> value = entry.getValue();
      if (value==placeholder)
        value=previousPlaceholder;
      newPlaceholdersWhenModified.put(entry.getKey(), value);
    }
    placeholdersWhenModified = newPlaceholdersWhenModified;
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