package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MixinsScope {
  
  private Map<String, List<FullMixinDefinition>> storage = new HashMap<String, List<FullMixinDefinition>>();
  
  public MixinsScope() {
  }

  public void registerMixin(FullMixinDefinition mixin) {
    String name = mixin.getMixin().getName();
    List<FullMixinDefinition> list = storage.get(name);
    if (list == null) {
      list = new ArrayList<FullMixinDefinition>();
      storage.put(name, list);
    }
    list.add(mixin);
  }

  public List<FullMixinDefinition> getMixins(String name) {
    return storage.get(name);
  }

  public boolean contains(String name) {
    return storage.containsKey(name);
  }

  @Override
  protected MixinsScope clone() {
    //FIXME: this cloning method is suspicios, either I do not need clonning or I need really deep clonning - is it ever used?
    try {
      MixinsScope result = (MixinsScope) super.clone();
      result.storage = new HashMap<String, List<FullMixinDefinition>>(storage);
      return result;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("This should never happen.", e);
    }
  }

  public int size() {
    return storage.size();
  }
}
