package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.github.sommeri.less4j.core.ast.AbstractVariableDeclaration;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionFilter;

public class VariablesDeclarationsStorage {

  private int registeredPlaceholders = 0;
  private int usedPlaceholders = 0;
  private Map<String, Integer> placeholdersWhenModified = new HashMap<String, Integer>();
  private Map<String, Expression> variables = new HashMap<String, Expression>();

  public VariablesDeclarationsStorage() {
  }

  public Expression getValue(String name) {
    return variables.get(name);
  }

  public void registerPlaceholder() {
    registeredPlaceholders+=1;
  }

  public void addToPlaceholder(VariablesDeclarationsStorage otherStorage) {
    Map<String, Expression> otherVariables = otherStorage.variables;
    for (Entry<String, Expression> entry : otherVariables.entrySet()) {
      String name = entry.getKey();
      Expression value = entry.getValue();
      if (variables.containsKey(name)) {
        int position = placeholdersWhenModified.get(name);
        if (position<=usedPlaceholders)
          addDeclaration(name, value);
      } else {
        addDeclaration(name, value);
      }
    }
  }

  public void closePlaceholder() {
    usedPlaceholders+=1;
  }

  public void addDeclaration(AbstractVariableDeclaration node) {
    addDeclaration(node.getVariable().getName(), node.getValue());
  }

  public void addDeclaration(AbstractVariableDeclaration node, Expression replacementValue) {
    addDeclaration(node.getVariable().getName(), replacementValue);
  }

  public void addDeclaration(String name, Expression replacementValue) {
    variables.put(name, replacementValue);
    placeholdersWhenModified.put(name, registeredPlaceholders);
  }

  public void addDeclarationIfNotPresent(String name, Expression replacementValue) {
    if (!variables.containsKey(name))
      addDeclaration(name, replacementValue);
  }

  public void removeDeclaration(String name) {
    variables.remove(name);
  }

  public void fillByFilteredVariables(ExpressionFilter filter, VariablesDeclarationsStorage variablesSource) {
    for (Entry<String, Expression> entry : variablesSource.variables.entrySet()) {
      String name = entry.getKey();
      Expression value = entry.getValue();
      addDeclaration(name, filter.apply(value));
    }
  }

  public int size() {
    return variables.size();
  }

}