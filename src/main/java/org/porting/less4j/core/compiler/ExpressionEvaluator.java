package org.porting.less4j.core.compiler;

import org.porting.less4j.core.ast.CssString;
import org.porting.less4j.core.ast.Expression;
import org.porting.less4j.core.ast.IndirectVariable;
import org.porting.less4j.core.ast.Variable;

public class ExpressionEvaluator {
  
  private final ActiveVariableScope variableScope;
  
  public ExpressionEvaluator(ActiveVariableScope variableScope) {
    super();
    this.variableScope = variableScope;
  }

  public Expression evaluate(Variable input) {
    Expression value = variableScope.getDeclaredValue(input);
    return evaluate(value);
  }

  public Expression evaluate(IndirectVariable input) {
    Expression value = variableScope.getDeclaredValue(input);
    CssString realName = convertToStringExpression(evaluate(value));
    return evaluate(variableScope.getDeclaredValue("@"+realName.getValue(), realName));
  }

  private CssString convertToStringExpression(Expression evaluate) {
    // FIXME: definitely not finished
    // TODO error handling
    return (CssString)evaluate;
  }

  public Expression evaluate(Expression input) {
    return input;
  }

}
