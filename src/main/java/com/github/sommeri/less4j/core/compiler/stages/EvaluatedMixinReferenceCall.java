package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionEvaluator;

public class EvaluatedMixinReferenceCall {
  
  private final MixinReference reference;
  private final List<Expression> positionalParameters = new ArrayList<Expression>();
  private final Map<String, Expression> namedParameters = new HashMap<String, Expression>();
  
  public EvaluatedMixinReferenceCall(MixinReference reference, ExpressionEvaluator evaluator) {
    super();
    this.reference = reference;
    
    for (Expression expression : reference.getPositionalParameters()) {
      positionalParameters.add(evaluator.evaluate(expression));
    }
    
    for (Entry<String, Expression> entry : reference.getNamedParameters().entrySet()) {
      namedParameters.put(entry.getKey(), evaluator.evaluate(entry.getValue()));
    }
  }

  public MixinReference getReference() {
    return reference;
  }

  public List<Expression> getPositionalParameters() {
    return positionalParameters;
  }

  public Expression getPositionalParameter(int parameterNumber) {
    return getPositionalParameters().get(parameterNumber);
  }
  
  public boolean hasPositionalParameter(int parameterNumber) {
    return getPositionalParameters().size() > parameterNumber;
  }

  public int getNumberOfDeclaredParameters() {
    return positionalParameters.size() + namedParameters.size();
  }

  public boolean hasNamedParameter(Variable variable) {
    return namedParameters.containsKey(variable.getName());
  }

  public Expression getNamedParameter(Variable variable) {
    return namedParameters.get(variable.getName());
  }

}
