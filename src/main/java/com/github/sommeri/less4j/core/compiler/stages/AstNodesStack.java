package com.github.sommeri.less4j.core.compiler.stages;

import java.util.Stack;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class AstNodesStack {
  
  private Stack<HiddenTokenAwareTree> currentlyCompiling = new Stack<HiddenTokenAwareTree>();
  
  public void pop() {
    currentlyCompiling.pop();
  }

  public void push(ASTCssNode node) {
    currentlyCompiling.push(node.getUnderlyingStructure());
  }

  public boolean contains(ASTCssNode node) {
    return currentlyCompiling.contains(node.getUnderlyingStructure());
  }

}
