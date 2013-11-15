package com.github.sommeri.less4j.core.compiler.stages;

import java.util.Stack;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

/** 
 * 
 * TODO: this assumes that two nodes have the same underlying structure if and only if they represent the same node in antlr ast.
 * It is not true generally, but works for namespace cycle detection purposes. It would be better, if we could clean 
 * that up somehow.
 */
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
