package com.github.sommeri.less4j.core.compiler.expressions;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class GuardsAndPatternsValidator {
  
  private final DefaultPoweredExpressionEvaluator  ifDefaultExpressionEvaluator;
  private final DefaultPoweredExpressionEvaluator  ifNotDefaultExpressionEvaluator;
  private final ExpressionEvaluator argumentsEvaluator;
  private ExpressionComparator patternsComparator = new PatternsComparator();

  public GuardsAndPatternsValidator(IScope scope, ProblemsHandler problemsHandler) {
    ifDefaultExpressionEvaluator = new DefaultPoweredExpressionEvaluator(scope, problemsHandler, true);
    ifNotDefaultExpressionEvaluator = new DefaultPoweredExpressionEvaluator(scope, problemsHandler, false);
    argumentsEvaluator = new ExpressionEvaluator(scope, problemsHandler);
  }

  public boolean guardsSatisfied(ReusableStructure mixin, boolean assumeDefault) {
    if (assumeDefault)
      return ifDefaultExpressionEvaluator.guardsSatisfied(mixin);
    
    return ifNotDefaultExpressionEvaluator.guardsSatisfied(mixin);
  }

  public boolean patternsMatch(MixinReference reference, ReusableStructure mixin) {
    int i = 0;
    for (ASTCssNode parameter : mixin.getParameters()) {
      if (parameter instanceof Expression) {
        if (!reference.hasPositionalParameter(i))
          return false;

        Expression pattern = (Expression) parameter;
        if (!patternsComparator.equal(pattern, argumentsEvaluator.evaluate(reference.getPositionalParameter(i))))
          return false;
      }
      i++;
    }
    
    return true;
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
