package com.github.sommeri.less4j.core.compiler.scopes;

import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.compiler.scopes.refactoring.BasicScope;

public class ScopeDecorator extends BasicScope {

  private final IScope decoree;  
  
  //FIXME: (!!!) do naming correctly
  protected ScopeDecorator(IScope decoree) {
    super(decoree, decoree.getSurroundingScopes());
    this.decoree = decoree;
  }

  public Expression getLocalValue(Variable variable) {
    return decoree.getLocalValue(variable);
  }

  public Expression getLocalValue(String name) {
    return decoree.getLocalValue(name);
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder(toFullName());
    result.append("\n\n").append(decoree.toString());
    return result.toString();
  }

}
