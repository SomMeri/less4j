package com.github.sommeri.less4j.core.compiler.stages;

import java.util.Stack;

import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;


public class MixinsCycleDetector {

  private Stack<HiddenTokenAwareTree> stack = new Stack<HiddenTokenAwareTree>();

  protected MixinsCycleDetector() {
  }
  
  public CycleType detectCycle(ReusableStructure mixin) {
    if (stack.contains(mixin.getUnderlyingStructure()))
      return CycleType.FAULTY;
    
    return CycleType.NONE;
  }

  public void entering(ReusableStructure mixin) {
    stack.push(mixin.getUnderlyingStructure());
  }

  public void leaving(ReusableStructure referencedMixin) {
    stack.pop();
  }

  public enum CycleType {
    LEGITIMATE, FAULTY, NONE;
  }

}

