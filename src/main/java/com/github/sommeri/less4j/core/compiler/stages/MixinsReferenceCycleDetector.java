package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import com.github.sommeri.less4j.core.ast.MixinReference;

@Deprecated
public class MixinsReferenceCycleDetector {

  private Stack<String> names = new Stack<String>();
  private Stack<MixinReference> variables = new Stack<MixinReference>();

  protected MixinsReferenceCycleDetector() {
  }

  public boolean wouldCycle(MixinReference input) {
    return names.contains(input.getName());
  }
  
  public void leftVariableValue() {
    names.pop();
    variables.pop();
  }

  public void enteringMixinReference(MixinReference input) {
    names.add(input.getName());
    variables.add(input);
  }

  public List<MixinReference> getCycleFor(MixinReference input) {
    if (!wouldCycle(input))
      return Collections.emptyList();
    
    int position = names.indexOf(input.getName());
    List<MixinReference> result = new ArrayList<MixinReference>(variables.subList(position, variables.size()));
    result.add(input);
    return result;
  }

}

class ReferenceId {
  
  
  protected ReferenceId(MixinReference reference) {
    reference.getSourceLine();
    reference.getSourceLine();
  }
}
