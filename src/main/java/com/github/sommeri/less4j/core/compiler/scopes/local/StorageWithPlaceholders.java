package com.github.sommeri.less4j.core.compiler.scopes.local;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class StorageWithPlaceholders<T> implements Cloneable {

  //FIXME: !!!!!!!!!! maybe remove this?
  private int registeredPlaceholders = 0;
  private int usedPlaceholders = 0;
  private Map<String, Integer> placeholdersWhenModified = new HashMap<String, Integer>();
  private List<StoragePlaceholder<T>> placeholders = new ArrayList<StorageWithPlaceholders.StoragePlaceholder<T>>();

  public StoragePlaceholder<T> createPlaceholder() {
    StoragePlaceholder<T> placeholder = new StoragePlaceholder<T>();
    placeholders.add(placeholder);
    registeredPlaceholders += 1;
    return placeholder;
  }

  public void closePlaceholder() {
    usedPlaceholders += 1;
  }

  private int getPosition(String name) {
    Integer position = placeholdersWhenModified.get(name);
    return position == null ? -1 : position;
  }

  protected int getPosition(StoragePlaceholder<T> placeholder) {
    return placeholders.indexOf(placeholder);
  }
  
  protected StoragePlaceholder<T> getFirstUnusedPlaceholder() {
    return placeholders.get(usedPlaceholders);
  }

  public void store(String name, T value) {
    placeholdersWhenModified.put(name, registeredPlaceholders);
    doStore(name, value);
  }

  public void store(String name, List<T> value) {
    placeholdersWhenModified.put(name, registeredPlaceholders);
    doStore(name, value);
  }

  protected List<StoragePlaceholder<T>> getPlaceholders() {
    return placeholders;
  }

  protected abstract void doStore(String name, T value);

  protected abstract void doStore(String name, List<T> value);

  protected boolean storedUnderPlaceholder(String name, StoragePlaceholder<T> placeholder) {
    return getPosition(name) <= getPosition(placeholder); // - maybe normally count the position in the list?
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

  public static class StoragePlaceholder<T> {
    
    public StoragePlaceholder() {
      super();
    }

  }
}
