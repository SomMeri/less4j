package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.sommeri.less4j.core.ast.ReusableStructureName;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class MixinsDefinitionsStorage implements Cloneable {

  private Map<String, List<FullMixinDefinition>> storage = new HashMap<String, List<FullMixinDefinition>>();
  private List<Placeholder> placeholders = new ArrayList<Placeholder>();
  private List<Placeholder> usedPlaceholders = new ArrayList<Placeholder>();
  
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
  }

  protected void doStore(String name, List<FullMixinDefinition> mixins) {
    List<FullMixinDefinition> list = getStoredList(name);
    list.addAll(mixins);
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
    Placeholder placeholder = placeholders.get(0);

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

  //FIXME: !!!! clean up!!!!
  @Deprecated
  protected Map<String, List<FullMixinDefinition>> getStorage() {
    return storage;
  }

  public void createPlaceholder() {
    placeholders.add(new Placeholder(this));
  }

  public void closePlaceholder() {
    Placeholder closing = placeholders.remove(0);
    usedPlaceholders.add(closing);
    
    if (placeholders.isEmpty())
      return ;
    
    Placeholder nextPlaceholder = placeholders.get(0);
    nextPlaceholder.activate(closing);
  }

  public String placeholdersReport() {
    return "unused: " + placeholders.size() + " used: " + usedPlaceholders.size();
  }

  public MixinsDefinitionsStorage clone() {
    try {
      MixinsDefinitionsStorage clone = (MixinsDefinitionsStorage) super.clone();
      clone.storage = new HashMap<String, List<FullMixinDefinition>>(storage);
      clone.placeholders = clonePlaceholders(placeholders, clone);
      clone.usedPlaceholders = clonePlaceholders(usedPlaceholders, clone);
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new IllegalStateException("Impossible state.");
    }
  }

  protected List<Placeholder> clonePlaceholders(List<Placeholder> placeholders, MixinsDefinitionsStorage ownerClone) {
    List<Placeholder> result = new ArrayList<Placeholder>();
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
  private Map<String, FullMixinDefinition> previousMixin = new HashMap<String, FullMixinDefinition>();

  protected Placeholder(MixinsDefinitionsStorage owner) {
    this.owner = owner;
    for (Entry<String, List<FullMixinDefinition>> entry : owner.getStorage().entrySet()) {
      String name = entry.getKey();
      List<FullMixinDefinition> mixins = entry.getValue();
      previousMixin.put(name, ArraysUtils.last(mixins));
    }
  }

  protected void activate(Placeholder closingPlaceholder) {
    //FIXME: !!!! another ugly place - it would be cool if this would not be needed at all - storing nextMixin istead of previous one is much better solution
    for (Entry<String, FullMixinDefinition> entry : new HashSet<Entry<String, FullMixinDefinition>>(closingPlaceholder.previousMixin.entrySet())) {
      String name = entry.getKey();
      List<FullMixinDefinition> storedList = owner.getStoredList(name);

      FullMixinDefinition myPreviousMixin = entry.getValue();
      int myPosition = position(myPreviousMixin, storedList);

      FullMixinDefinition closingPreviousMixin = closingPlaceholder.previousMixin.get(name);
      if (closingPreviousMixin!=null) {
        int closingPosition = position(closingPreviousMixin, storedList);
      
        if (myPosition <= closingPosition)
          previousMixin.put(name, closingPreviousMixin);
      }
    }
  }

  public void addToSelf(String name, List<FullMixinDefinition> values) {
    List<FullMixinDefinition> storedList = owner.getStoredList(name);
    int position = position(name, storedList);
    
    storedList.addAll(position, values);
    previousMixin.put(name, ArraysUtils.last(values));
  }

  private int position(String name, List<FullMixinDefinition> storedList) {
    FullMixinDefinition mixin = previousMixin.get(name);
    return position(mixin, storedList);
  }

  protected int position(FullMixinDefinition mixin, List<FullMixinDefinition> storedList) {
    if (mixin == null)
      return 0;

    return storedList.indexOf(mixin) + 1;
  }

  @Override
  protected Placeholder clone() {
    try {
      Placeholder clone = (Placeholder) super.clone();
      clone.previousMixin = new HashMap<String, FullMixinDefinition>(previousMixin);
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
