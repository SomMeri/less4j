package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.sommeri.less4j.core.ast.ReusableStructureName;


public class MixinsDefinitionsStorage {
  
  private Map<String, List<FullMixinDefinition>> storage = new HashMap<String, List<FullMixinDefinition>>();
  
  public MixinsDefinitionsStorage() {
  }

  public void registerMixin(FullMixinDefinition mixin) {
    List<ReusableStructureName> names = mixin.getMixin().getNames();
    for (ReusableStructureName name : names) {
      registerMixin(name, mixin);
    }
  }

  private void registerMixin(ReusableStructureName name, FullMixinDefinition mixin) {
    String nameAsString = name.asString();
    List<FullMixinDefinition> list = storage.get(nameAsString);
    if (list == null) {
      list = new ArrayList<FullMixinDefinition>();
      storage.put(nameAsString, list);
    }
    list.add(mixin);
  }

  public List<FullMixinDefinition> getMixins(ReusableStructureName name) {
    return storage.get(name.asString());
  }

  public boolean contains(ReusableStructureName name) {
    return storage.containsKey(name.asString());
  }

  public int size() {
    return storage.size();
  }
}
