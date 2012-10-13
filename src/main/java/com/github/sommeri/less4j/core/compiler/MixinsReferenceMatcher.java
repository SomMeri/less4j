package com.github.sommeri.less4j.core.compiler;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.PureMixin;

public class MixinsReferenceMatcher {

  public List<MixinWithVariablesState> filter(MixinReference reference, List<MixinWithVariablesState> list) {
    return filterByParametersNumber(reference, list);
  }

  private List<MixinWithVariablesState> filterByParametersNumber(MixinReference reference, List<MixinWithVariablesState> list) {
    int requiredNumber = reference.getParameters().size();
    List<MixinWithVariablesState> result = new ArrayList<MixinWithVariablesState>();
    for (MixinWithVariablesState MixinWithVariablesState : list) {
      PureMixin mixin = MixinWithVariablesState.getMixin();
      int allDefined = mixin.getParameters().size();
      int mandatory = mixin.getMandatoryParameters().size();
      if (requiredNumber >= mandatory && (requiredNumber <= allDefined || mixin.hasCollectorParameter()))
        result.add(MixinWithVariablesState);
    }
    return result;
  }

}
