package com.github.sommeri.less4j.core.compiler.scopes.local;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.sommeri.less4j.utils.ArraysUtils;
import com.github.sommeri.less4j.utils.PubliclyCloneable;

public class KeyValueStorage<M, T> implements Cloneable {

  private LinkedList<Level<M, T>> levels = new LinkedList<Level<M, T>>();
  private LinkedList<ValuePlaceholder<M, T>> placeholders = new LinkedList<ValuePlaceholder<M, T>>();

  public int size() {
    return levels.size();
  }

  public void add(M key, T thing) {
    Level<M, T> lastLevel = getLastLevel();
    lastLevel.add(key, thing);
  }

  public void add(KeyValueStorage<M, T> otherStorage) {
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

  public T getValue(M key) {
    Iterator<Level<M, T>> di = levels.descendingIterator();
    while (di.hasNext()) {
      Level<M, T> level = di.next();
      if (level.contains(key))
        return level.getValue(key);
    }

    return null;
  }

  public Set<Entry<M, T>> getAllEntries() {
    Set<Entry<M, T>> result = new HashSet<Entry<M,T>>();
    Iterator<Level<M, T>> iterator = levels.descendingIterator();
    while (iterator.hasNext()) {
      Level<M, T> level = iterator.next();
      result.addAll(level.getAllEntries());
    }

    return result;
  }


  public ValuePlaceholder<M, T> createPlaceholder() {
    Level<M, T> addLevel = addLevel();
    ValuePlaceholder<M, T> placeholder = new ValuePlaceholder<M, T>(addLevel);
    placeholders.add(placeholder);
    // add level that will be on top of placeholder
    addLevel();
    return placeholder;
  }

  public void addDataToFirstPlaceholder(KeyValueStorage<M, T> otherStorage) { // used to be called addToPlaceholder
    ValuePlaceholder<M, T> placeholder = placeholders.peekFirst();
    addDataOnly(placeholder, otherStorage);
  }

  private void addDataOnly(ValuePlaceholder<M, T> placeholder, KeyValueStorage<M, T> otherStorage) {
    for (Level<M, T> level : otherStorage.levels) {
      placeholder.level.addAll(level);
    }
  }

  public void addToFirstPlaceholder(M key, T value) {
    ValuePlaceholder<M, T> placeholder = placeholders.peekFirst();
    placeholder.level.add(key, value);
  }

  public void closeFirstPlaceholder() { // used to be called closePlaceholder
    placeholders.pop();
  }

  //REPLACE whatever was stored in placeholder
  public void replacePlaceholder(ValuePlaceholder<M, T> placeholder, KeyValueStorage<M, T> otherStorage) {
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
  public KeyValueStorage<M, T> clone() {
    try {
      @SuppressWarnings("unchecked")
      KeyValueStorage<M, T> clone = (KeyValueStorage<M, T>) super.clone();
      clone.levels = ArraysUtils.deeplyClonedLinkedList(levels);
      clone.placeholders = new LinkedList<ValuePlaceholder<M, T>>();
      for (ValuePlaceholder<M, T> placeholder : placeholders) {
        int index = levels.indexOf(placeholder.level);
        Level<M, T> levelClone = clone.levels.get(index);
        clone.placeholders.add(new ValuePlaceholder<M, T>(levelClone));
      }
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new IllegalStateException("Impossible state.");
    }
  }

  @Override
  public String toString() {
    return levels.toString();
  }
  private static class Level<M, T> implements PubliclyCloneable {

    private Map<M, T> storage = new HashMap<M, T>();

    public void add(M key, T thing) {
      storage.put(key, thing);
    }

    public Collection<Entry<M, T>> getAllEntries() {
      return storage.entrySet();
    }

    public T getValue(M key) {
      return storage.get(key);
    }

    public boolean contains(M key) {
      return storage.containsKey(key);
    }

    public void addAll(Level<M, T> otherLevel) {
      for (Entry<M, T> entry : otherLevel.storage.entrySet()) {
        add(entry.getKey(), entry.getValue());
      }
    }

    @Override
    public Level<M, T> clone() {
      try {
        @SuppressWarnings("unchecked")
        Level<M, T> clone = (Level<M, T>) super.clone();
        //should I creat also new lists? old versoin have not done that
        clone.storage = new HashMap<M, T>(storage);
        return clone;
      } catch (CloneNotSupportedException e) {
        throw new IllegalStateException("Impossible state.");
      }
    }

    
    @Override
    public String toString() {
      return "Level: " + storage.toString();
    }
  }

  public static class ValuePlaceholder<M, T> {
    private final Level<M, T> level;

    public ValuePlaceholder(Level<M, T> level) {
      super();
      this.level = level;
    }

  }

}
