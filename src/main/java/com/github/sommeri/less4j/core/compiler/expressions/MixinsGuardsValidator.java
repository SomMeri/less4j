package com.github.sommeri.less4j.core.compiler.expressions;

import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class MixinsGuardsValidator {
  
  private final DefaultPoweredExpressionEvaluator  ifDefaultExpressionEvaluator;
  private final DefaultPoweredExpressionEvaluator  ifNotDefaultExpressionEvaluator;

  public MixinsGuardsValidator(IScope scope, ProblemsHandler problemsHandler) {
    ifDefaultExpressionEvaluator = new DefaultPoweredExpressionEvaluator(scope, problemsHandler, true);
    ifNotDefaultExpressionEvaluator = new DefaultPoweredExpressionEvaluator(scope, problemsHandler, false);
  }

  public boolean guardsSatisfied(ReusableStructure mixin, boolean assumeDefault) {
    if (assumeDefault)
      return ifDefaultExpressionEvaluator.guardsSatisfied(mixin);
    
    return ifNotDefaultExpressionEvaluator.guardsSatisfied(mixin);
  }

  private class DefaultPoweredExpressionEvaluator extends ExpressionEvaluator {
    public DefaultPoweredExpressionEvaluator(IScope scope, ProblemsHandler problemsHandler, boolean assumeDefault) {
      super(scope, problemsHandler);
      addFunctionsPack(new GuardOnlyFunctions(problemsHandler, assumeDefault));
    }

    public DefaultPoweredExpressionEvaluator(ProblemsHandler problemsHandler, boolean assumeDefault) {
      super(problemsHandler);
      addFunctionsPack(new GuardOnlyFunctions(problemsHandler, assumeDefault));
    }
  }

}
