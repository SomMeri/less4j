package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.sommeri.less4j.core.ast.ReusableStructureName;

public class MixinsDefinitionsStorage implements Cloneable {

  private Map<String, List<FullMixinDefinition>> storage = new HashMap<String, List<FullMixinDefinition>>();
  private LinkedList<Placeholder> openPlaceholders = new LinkedList<Placeholder>();
  
  public MixinsDefinitionsStorage() {
  }

  public void store(FullMixinDefinition mixin) {
    List<ReusableStructureName> names = mixin.getMixin().getNames();
    for (ReusableStructureName name : names) {
      doStore(name.asString(), mixin);
    }
  }

  public void store(String name, List<FullMixinDefinition> value) {
    doStore(name, value);
  }

  public void storeAll(List<FullMixinDefinition> mixins) {
    for (FullMixinDefinition mixin : mixins) {
      store(mixin);
    }
  }

  public void storeAll(MixinsDefinitionsStorage otherStorage) {
    for (List<FullMixinDefinition> list : otherStorage.storage.values()) {
      storeAll(list);
    }
  }

  public List<FullMixinDefinition> getMixins(ReusableStructureName name) {
    return storage.get(name.asString());
  }

  public List<FullMixinDefinition> getAllMixins() {
    List<FullMixinDefinition> result = new ArrayList<FullMixinDefinition>();
    for (List<FullMixinDefinition> value : storage.values()) {
      result.addAll(value);
    }
    return result;
  }

  protected void doStore(String name, FullMixinDefinition mixin) {
    List<FullMixinDefinition> list = getStoredList(name);
    list.add(mixin);
    
    updatePlaceholdersPositions(name, mixin);
  }

  protected void doStore(String name, List<FullMixinDefinition> mixins) {
    List<FullMixinDefinition> list = getStoredList(name);
    list.addAll(mixins);
    
    updatePlaceholdersPositions(name, mixins);
  }

  private void updatePlaceholdersPositions(String name, List<FullMixinDefinition> mixins) {
    if (!mixins.isEmpty())
      updatePlaceholdersPositions(name, mixins.get(0));
  }

  private void updatePlaceholdersPositions(String name, FullMixinDefinition mixin) {
    Iterator<Placeholder> iterator = openPlaceholders.descendingIterator();
    if (!iterator.hasNext())
      return ;
    
    Placeholder placeholder = iterator.next();
    while (!placeholder.knowPosition(name)) {
      placeholder.setPosition(name, mixin);
      
      // we can safely stay where we are if there is no next 
      if (iterator.hasNext())
        placeholder = iterator.next();
    }
    
  }

  protected List<FullMixinDefinition> getStoredList(String name) {
    List<FullMixinDefinition> list = storage.get(name);
    if (list == null) {
      list = new ArrayList<FullMixinDefinition>();
      storage.put(name, list);
    }
    return list;
  }

  public boolean contains(ReusableStructureName name) {
    return contains(name.asString());
  }

  public boolean contains(String name) {
    return storage.containsKey(name);
  }

  public int size() {
    return storage.size();
  }

  public void addToPlaceholder(MixinsDefinitionsStorage otherStorage) {
    Placeholder placeholder = openPlaceholders.peekFirst();

    Map<String, List<FullMixinDefinition>> otherMixins = otherStorage.storage;
    for (Entry<String, List<FullMixinDefinition>> entry : otherMixins.entrySet()) {
      String name = entry.getKey();
      List<FullMixinDefinition> values = entry.getValue();
      placeholder.addToSelf(name, values);
    }
  }

  public void addAll(MixinsDefinitionsStorage source) {
    for (Entry<String, List<FullMixinDefinition>> entry : source.storage.entrySet()) {
      String name = entry.getKey();
      List<FullMixinDefinition> values = entry.getValue();
      store(name, values);
    }

  }

  public void createPlaceholder() {
    openPlaceholders.add(new Placeholder(this));
  }

  public void closePlaceholder() {
    openPlaceholders.pop();
  }

  public String placeholdersReport() {
    return "unused: " + openPlaceholders.size() + " used: ";
  }

  public MixinsDefinitionsStorage clone() {
    try {
      MixinsDefinitionsStorage clone = (MixinsDefinitionsStorage) super.clone();
      clone.storage = new HashMap<String, List<FullMixinDefinition>>(storage);
      clone.openPlaceholders = clonePlaceholders(openPlaceholders, clone);
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new IllegalStateException("Impossible state.");
    }
  }

  protected LinkedList<Placeholder> clonePlaceholders(LinkedList<Placeholder> placeholders, MixinsDefinitionsStorage ownerClone) {
    LinkedList<Placeholder> result = new LinkedList<Placeholder>();
    for (Placeholder placeholder : placeholders) {
      result.add(placeholder.clone(ownerClone));
    }
    return result;
  }
  
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder(getClass().getSimpleName()).append("\n");;
    result.append("Mixins: ").append(storage.keySet());
    return result.toString();
  }

}

class Placeholder implements Cloneable {

  private MixinsDefinitionsStorage owner;
  private Map<String, FullMixinDefinition> nextMixin = new HashMap<String, FullMixinDefinition>();

  protected Placeholder(MixinsDefinitionsStorage owner) {
    this.owner = owner;
  }

  public boolean knowPosition(String name) {
    return nextMixin.containsKey(name);
  }

  public void setPosition(String name, FullMixinDefinition mixin) {
    nextMixin.put(name, mixin);
  }

  public void addToSelf(String name, List<FullMixinDefinition> values) {
    List<FullMixinDefinition> storedList = owner.getStoredList(name);
    int position = position(name, storedList);
    
    storedList.addAll(position, values);
  }

  private int position(String name, List<FullMixinDefinition> storedList) {
    FullMixinDefinition mixin = nextMixin.get(name);
    return position(mixin, storedList);
  }

  protected int position(FullMixinDefinition mixin, List<FullMixinDefinition> storedList) {
    if (mixin == null)
      return storedList.size(); // end of list

    return storedList.indexOf(mixin);
  }

  @Override
  protected Placeholder clone() {
    try {
      Placeholder clone = (Placeholder) super.clone();
      clone.nextMixin = new HashMap<String, FullMixinDefinition>(nextMixin);
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new IllegalStateException("Impossible state.");
    }
  }

  protected Placeholder clone(MixinsDefinitionsStorage owner) {
    Placeholder clone = clone();
    clone.owner = owner;
    return clone;
  }

}
