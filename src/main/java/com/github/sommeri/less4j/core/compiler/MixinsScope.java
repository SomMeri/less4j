package com.github.sommeri.less4j.core.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MixinsScope {
  
  private Map<String, List<FullMixinDefinition>> storage = new HashMap<String, List<FullMixinDefinition>>();
  
  public MixinsScope() {
  }

  public MixinsScope(MixinsScope scope) {
    storage = new HashMap<String, List<FullMixinDefinition>>(scope.storage);
  }

  public void registerMixin(FullMixinDefinition mixinWithState) {
    String name = mixinWithState.getMixin().getName();
    List<FullMixinDefinition> list = storage.get(name);
    if (list == null) {
      list = new ArrayList<FullMixinDefinition>();
      storage.put(name, list);
    }
    list.add(mixinWithState);
  }

  public List<FullMixinDefinition> getMixins(String name) {
    return storage.get(name);
  }

  public boolean contains(String name) {
    return storage.containsKey(name);
  }

}
