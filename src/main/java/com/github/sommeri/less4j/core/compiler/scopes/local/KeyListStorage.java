package com.github.sommeri.less4j.core.compiler.scopes.local;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.sommeri.less4j.utils.ArraysUtils;
import com.github.sommeri.less4j.utils.PubliclyCloneable;

public class KeyListStorage<M, T> implements Cloneable {

  private LinkedList<Level<M, T>> levels = new LinkedList<Level<M, T>>();
  private LinkedList<ListPlaceholder<M, T>> placeholders = new LinkedList<ListPlaceholder<M, T>>();

  public void add(M key, T thing) {
    Level<M, T> lastLevel = getLastLevel();
    lastLevel.add(key, thing);
  }

  public void add(M key, List<T> thing) {
    Level<M, T> lastLevel = getLastLevel();
    lastLevel.add(key, thing);
  }

  public void add(ListPlaceholder<M, T> placeholder, M key, List<T> thing) {
    placeholder.level.add(key, thing);
  }

  public void add(KeyListStorage<M, T> otherStorage) {
    levels.addAll(otherStorage.levels);
    placeholders.addAll(otherStorage.placeholders);
  }

  public boolean contains(M key) {
    for (Level<M, T> level : levels) {
      if (level.contains(key))
        return true;
    }
    return false;
  }

  public List<T> getValues(M key) {
    LinkedList<T> result = new LinkedList<T>();
    for (Level<M, T> level : levels) {
      result.addAll(level.getValues(key));
    }

    return result;
  }

  public List<T> getAllValues() {
    LinkedList<T> result = new LinkedList<T>();
    for (Level<M, T> level : levels) {
      result.addAll(level.getAllValues());
    }

    return result;
  }

  public ListPlaceholder<M, T> createPlaceholder() {
    Level<M, T> addLevel = addLevel();
    ListPlaceholder<M, T> placeholder = new ListPlaceholder<M, T>(addLevel);
    placeholders.add(placeholder);
    // add level that will be on top of placeholder
    addLevel(); 
    return placeholder;
  }

  public void addDataToFirstPlaceholder(KeyListStorage<M, T> otherStorage) { // used to be called addToPlaceholder
    ListPlaceholder<M, T> placeholder = placeholders.peekFirst();
    addDataOnly(placeholder, otherStorage);
  }

  private void addDataOnly(ListPlaceholder<M, T> placeholder, KeyListStorage<M, T> otherStorage) {
    for (Level<M, T> level : otherStorage.levels) {
      placeholder.level.addAll(level);
    }
  }

  public void closeFirstPlaceholder() { // used to be called closePlaceholder
    placeholders.pop();
  }

  //REPLACE whatever was stored in placeholder
  public void replacePlaceholder(ListPlaceholder<M, T> placeholder, KeyListStorage<M, T> otherStorage) {
    //replace in data
    ArraysUtils.replace(levels, placeholder.level, otherStorage.levels);
    ArraysUtils.replace(placeholders, placeholder, otherStorage.placeholders);
  }

  private Level<M, T> getLastLevel() {
    if (levels.isEmpty()) {
      addLevel();
    }

    Level<M, T> lastLevel = levels.peekLast();
    return lastLevel;
  }

  private Level<M, T> addLevel() {
    levels.add(new Level<M, T>());
    return levels.peekLast();
  }

  @Override
  public KeyListStorage<M, T> clone() {
    try {
      @SuppressWarnings("unchecked")
      KeyListStorage<M, T> clone = (KeyListStorage<M, T>) super.clone();
      clone.levels = ArraysUtils.deeplyClonedLinkedList(levels);
      clone.placeholders = new LinkedList<ListPlaceholder<M, T>>();
      for (ListPlaceholder<M, T> placeholder : placeholders) {
        int index = levels.indexOf(placeholder.level);
        Level<M, T> levelClone = clone.levels.get(index);
        clone.placeholders.add(new ListPlaceholder<M, T>(levelClone));
      }
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new IllegalStateException("Impossible state.");
    }
  }

  private static class Level<M, T> implements PubliclyCloneable {

    private Map<M, List<T>> storage = new HashMap<M, List<T>>();

    public void add(M key, T thing) {
      getStoredList(key).add(thing);
    }

    public void add(M key, List<T> things) {
      getStoredList(key).addAll(things);
    }

    public void addAll(Level<M, T> otherLevel) {
      for (Entry<M, List<T>> entry : otherLevel.storage.entrySet()) {
        add(entry.getKey(), entry.getValue());
      }
    }

    private List<T> getStoredList(M key) {
      List<T> list = storage.get(key);
      if (list == null) {
        list = new ArrayList<T>();
        storage.put(key, list);
      }
      return list;
    }

    public boolean contains(M key) {
      return storage.containsKey(key);
    }

    public List<T> getValues(M key) {
      return storage.containsKey(key) ? storage.get(key) : new ArrayList<T>();
    }

    public Collection<T> getAllValues() {
      List<T> result = new ArrayList<T>();
      for (List<T> list : storage.values()) {
        result.addAll(list);
      }
      return result;
    }

    @Override
    public Level<M, T> clone() {
      try {
        @SuppressWarnings("unchecked")
        Level<M, T> clone = (Level<M, T>) super.clone();
        //should I creat also new lists? old versoin have not done that
        clone.storage = new HashMap<M, List<T>>(storage);
        return clone;
      } catch (CloneNotSupportedException e) {
        throw new IllegalStateException("Impossible state.");
      }
    }

  }

  public static class ListPlaceholder<M, T> {
    private final Level<M, T> level;

    public ListPlaceholder(Level<M, T> level) {
      super();
      this.level = level;
    }

  }

}
