package com.github.sommeri.less4j.core.compiler;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.ast.VariableDeclaration;

/**
 * Not exactly memory effective, but lets create working version first.  
 *
 */
public class ActiveVariableScope {
  private Stack<Map<String, VariableDeclaration>> activeScope = new Stack<Map<String, VariableDeclaration>>();
  
  public ActiveVariableScope() {
    activeScope.push(new HashMap<String, VariableDeclaration>());
  }

  public void addDeclaration(VariableDeclaration node) {
    activeScope.peek().put(node.getVariable().getName(), (VariableDeclaration)node);
  }

  public void decreaseScope() {
    activeScope.pop();
  }

  public void increaseScope() {
    Map<String, VariableDeclaration> old = activeScope.peek();
    activeScope.push(new HashMap<String, VariableDeclaration>(old));
  }

  public Expression getDeclaredValue(Variable node) {
    String name = node.getName();
    VariableDeclaration declaration = activeScope.peek().get(name);
    if (declaration==null)
      throw new CompileException("The variable \"" +name+ "\" was not declared.", node);
    
    return declaration.getValue();
  }

  public Expression getDeclaredValue(String name, ASTCssNode ifErrorNode) {
    VariableDeclaration declaration = activeScope.peek().get(name);
    if (declaration==null)
      throw new CompileException("The variable \"" +name+ "\" was not declared.", ifErrorNode);
    
    return declaration.getValue();
  }

}
