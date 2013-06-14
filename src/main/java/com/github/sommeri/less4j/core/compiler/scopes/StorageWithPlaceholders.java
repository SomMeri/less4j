package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class StorageWithPlaceholders<T> implements Cloneable {

  private int registeredPlaceholders = 0;
  private int usedPlaceholders = 0;
  private Map<String, Integer> placeholdersWhenModified = new HashMap<String, Integer>();

  public void createPlaceholder() {
    registeredPlaceholders += 1;
  }

  public void closePlaceholder() {
    usedPlaceholders += 1;
  }

  private int getPosition(String name) {
    Integer position = placeholdersWhenModified.get(name);
    return position == null ? -1 : position;
  }

  protected int getUsedPlaceholders() {
    return usedPlaceholders;
  }

  public void store(String name, T value) {
    placeholdersWhenModified.put(name, registeredPlaceholders);
    doStore(name, value);
  }

  public void store(String name, List<T> value) {
    placeholdersWhenModified.put(name, registeredPlaceholders);
    doStore(name, value);
  }

  protected abstract void doStore(String name, T value);

  protected abstract void doStore(String name, List<T> value);

  protected boolean storedBeforeUnusedPlaceholder(String name) {
    return getPosition(name) <= getUsedPlaceholders();
  }

  public String placeholdersReport() {
    return "registered: " + registeredPlaceholders + " used: " + usedPlaceholders;
  }

  public StorageWithPlaceholders<T> clone() {
    try {
      @SuppressWarnings("unchecked")
      StorageWithPlaceholders<T> clone = (StorageWithPlaceholders<T>) super.clone();
      clone.placeholdersWhenModified = new HashMap<String, Integer>(placeholdersWhenModified);
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new IllegalStateException("Impossible state.");
    }
  }
}
