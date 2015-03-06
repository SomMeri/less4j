package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.LinkedList;

import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.compiler.expressions.GuardValue;

public class FoundMixin extends FullMixinDefinition {
  
  private LinkedList<GuardValue> guardsOnPath = new LinkedList<GuardValue>();

  public FoundMixin(ReusableStructure mixin, IScope mixinsBodyScope) {
    super(mixin, mixinsBodyScope);
  }

  public FoundMixin(FullMixinDefinition fullMixinDefinition) {
    super(fullMixinDefinition.getMixin(), fullMixinDefinition.getScope());
  }

  public void prefixGuardValue(GuardValue guardValue) {
    guardsOnPath.addFirst(guardValue);
  }

  public LinkedList<GuardValue> getGuardsOnPath() {
    return guardsOnPath;
  }

}
