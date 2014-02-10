package com.github.sommeri.less4j.core.compiler.scopes.local;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ReusableStructureName;
import com.github.sommeri.less4j.core.compiler.scopes.FullMixinDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.local.CoolStorage.Placeholder;

public class MixinsDefinitionsStorage implements Cloneable {

  private CoolStorage<String, FullMixinDefinition> coolStorage = new CoolStorage<String, FullMixinDefinition>();

  public MixinsDefinitionsStorage() {
  }

  public void store(FullMixinDefinition mixin) {
    List<ReusableStructureName> names = mixin.getMixin().getNames();
    for (ReusableStructureName name : names) {
      coolStorage.add(name.asString(), mixin);
    }
  }

  public void store(String name, List<FullMixinDefinition> value) {
    coolStorage.add(name, value);
  }

  public void storeAll(List<FullMixinDefinition> mixins) {
    for (FullMixinDefinition mixin : mixins) {
      store(mixin);
    }
  }

  public void storeAll(MixinsDefinitionsStorage otherStorage) {
    coolStorage.add(otherStorage.coolStorage);
  }

  public List<FullMixinDefinition> getMixins(List<String> nameChain, ReusableStructureName name) {
    return getMixins(toMixinName(nameChain, name));
  }

  private String toMixinName(List<String> nameChain, ReusableStructureName name) {
    StringBuilder result = new StringBuilder();
    for (String str : nameChain) {
      result.append(str);
    }
    result.append(name.asString());
    return result.toString();
  }

  public List<FullMixinDefinition> getMixins(ReusableStructureName name) {
    return getMixins(name.asString());
  }

  public List<FullMixinDefinition> getMixins(String name) {
    List<FullMixinDefinition> mixins = coolStorage.getValues(name);
    return mixins != null ? mixins : new ArrayList<FullMixinDefinition>();
  }

  public List<FullMixinDefinition> getAllMixins() {
    return coolStorage.getAllValues();
  }

  public void addToPlaceholder(MixinsDefinitionsStorage otherStorage) {
    coolStorage.addDataToFirstPlaceholder(otherStorage.coolStorage);
  }

  public void replacePlaceholder(MixinsPlaceholder mixinsPlaceholder, MixinsDefinitionsStorage otherStorage) {
    coolStorage.replacePlaceholder(mixinsPlaceholder.coolPlaceholder, otherStorage.coolStorage);
  }

  public void addAll(MixinsDefinitionsStorage source) {
    coolStorage.add(source.coolStorage);
  }

  public MixinsPlaceholder createPlaceholder() {
    Placeholder<String, FullMixinDefinition> coolPlaceholder = coolStorage.createPlaceholder();
    return new MixinsPlaceholder(coolPlaceholder);
  }

  public void closePlaceholder() {
    coolStorage.closeFirstPlaceholder();
  }

  public int size() {
    return coolStorage.getAllValues().size();
  }
  
  public MixinsDefinitionsStorage clone() {
    try {
      MixinsDefinitionsStorage clone = (MixinsDefinitionsStorage) super.clone();
      clone.coolStorage = coolStorage.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new IllegalStateException("Impossible state.");
    }
  }

  @Override
  public String toString() {
    return coolStorage.toString();
  }

  public static class MixinsPlaceholder {

    private final Placeholder<String, FullMixinDefinition> coolPlaceholder;

    public MixinsPlaceholder(Placeholder<String, FullMixinDefinition> coolPlaceholder) {
      this.coolPlaceholder = coolPlaceholder;
    }
    
  }

}

