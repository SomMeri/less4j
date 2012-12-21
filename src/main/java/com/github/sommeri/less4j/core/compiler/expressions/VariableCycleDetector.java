package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import com.github.sommeri.less4j.core.ast.Variable;

public class VariableCycleDetector {

  // since names are evaluated lazily, we can ignore the scope and consider variable name only
  private Stack<String> names = new Stack<String>();
  private Stack<Variable> variables = new Stack<Variable>();

  protected VariableCycleDetector() {
  }

  public boolean wouldCycle(Variable input) {
    return names.contains(input.getName());
  }
  
  public void leftVariableValue() {
    names.pop();
    variables.pop();
  }

  public void enteringVariableValue(Variable input) {
    names.add(input.getName());
    variables.add(input);
  }

  public List<Variable> getCycleFor(Variable input) {
    if (!wouldCycle(input))
      return Collections.emptyList();
    
    int position = names.indexOf(input.getName());
    List<Variable> result = new ArrayList<Variable>(variables.subList(position, variables.size()));
    result.add(input);
    return result;
  }

}
