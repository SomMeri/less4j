package com.github.sommeri.less4j.core.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MixinsScope {
  
  private Map<String, List<MixinWithVariablesState>> storage = new HashMap<String, List<MixinWithVariablesState>>();
  
  public MixinsScope() {
  }

  public MixinsScope(MixinsScope scope) {
    storage = new HashMap<String, List<MixinWithVariablesState>>(scope.storage);
  }

  public void registerMixin(MixinWithVariablesState mixinWithState) {
    String name = mixinWithState.getMixin().getName();
    List<MixinWithVariablesState> list = storage.get(name);
    if (list == null) {
      list = new ArrayList<MixinWithVariablesState>();
      storage.put(name, list);
    }
    list.add(mixinWithState);
  }

  public List<MixinWithVariablesState> getMixins(String name) {
    return storage.get(name);
  }

  public boolean contains(String name) {
    return storage.containsKey(name);
  }

}
