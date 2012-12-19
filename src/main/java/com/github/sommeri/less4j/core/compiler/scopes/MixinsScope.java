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
    List<String> names = mixin.getMixin().getNames();
    for (String name : names) {
      registerMixin(name, mixin);
    }
  }

  private void registerMixin(String name, FullMixinDefinition mixin) {
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

  public int size() {
    return storage.size();
  }
}
