package com.github.sommeri.less4j.core.compiler.remove;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
public class MixinsScopeOld {
  
  private Map<String, List<FullMixinDefinitionOld>> storage = new HashMap<String, List<FullMixinDefinitionOld>>();
  
  public MixinsScopeOld() {
  }

  public MixinsScopeOld(MixinsScopeOld scope) {
    storage = new HashMap<String, List<FullMixinDefinitionOld>>(scope.storage);
  }

  public MixinsScopeOld(MixinsScopeOld scope1, MixinsScopeOld scope2) {
    this(scope1);
    storage.putAll(scope2.storage);
  }

  public void registerMixin(FullMixinDefinitionOld mixinWithState) {
    String name = mixinWithState.getMixin().getName();
    List<FullMixinDefinitionOld> list = storage.get(name);
    if (list == null) {
      list = new ArrayList<FullMixinDefinitionOld>();
      storage.put(name, list);
    }
    list.add(mixinWithState);
  }

  public List<FullMixinDefinitionOld> getMixins(String name) {
    return storage.get(name);
  }

  public boolean contains(String name) {
    return storage.containsKey(name);
  }

  @Override
  protected MixinsScopeOld clone() {
    //FIXME: this cloning method is suspicios, either I do not need clonning or I need really deep clonning
    try {
      MixinsScopeOld result = (MixinsScopeOld) super.clone();
      result.storage = new HashMap<String, List<FullMixinDefinitionOld>>(storage);
      return result;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("This should never happen.", e);
    }
  }
}
