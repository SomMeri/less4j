package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.List;

import com.github.sommeri.less4j.LessCompiler.Configuration;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class MixinsGuardsValidator {

  private final DefaultPoweredExpressionEvaluator ifDefaultExpressionEvaluator;
  private final DefaultPoweredExpressionEvaluator ifNotDefaultExpressionEvaluator;

  public MixinsGuardsValidator(IScope scope, ProblemsHandler problemsHandler, Configuration configuration) {
    ifDefaultExpressionEvaluator = new DefaultPoweredExpressionEvaluator(scope, problemsHandler, configuration, true);
    ifNotDefaultExpressionEvaluator = new DefaultPoweredExpressionEvaluator(scope, problemsHandler, configuration, false);
  }

  public boolean guardsSatisfied(ReusableStructure mixin, boolean assumeDefault) {
    if (assumeDefault)
      return ifDefaultExpressionEvaluator.guardsSatisfied(mixin);

    return ifNotDefaultExpressionEvaluator.guardsSatisfied(mixin);
  }

  private class DefaultPoweredExpressionEvaluator extends ExpressionEvaluator {
    
    public DefaultPoweredExpressionEvaluator(IScope scope, ProblemsHandler problemsHandler, Configuration configuration, boolean assumeDefault) {
      super(scope, problemsHandler, configuration);
      addFunctionsPack(new GuardOnlyFunctions(problemsHandler, assumeDefault));
    }

  }

  public GuardValue evaluateGuards(ReusableStructure mixin) {
    boolean ifDefaultGuardValue = guardsSatisfied(mixin, true);
    boolean ifNotDefaultGuardValue = guardsSatisfied(mixin, false);
    return toDefaultFunctionUse(ifDefaultGuardValue, ifNotDefaultGuardValue);
  }

  /**
   * Re-implementing less.js heuristic. If guards value does not depend on default value, then less.js
   * assumes the default was not used. It does not check whether the default function was really used, so
   * this: not(default()), (default()) can be used multiple times.
   */
  private GuardValue toDefaultFunctionUse(boolean ifDefaultGuardValue, boolean ifNotDefaultGuardValue) {
    if (ifDefaultGuardValue && ifNotDefaultGuardValue) {//default was NOT used
      return GuardValue.USE;
    } else if (!ifDefaultGuardValue && !ifNotDefaultGuardValue) {//default was NOT used
      return GuardValue.DO_NOT_USE;
    } else if (ifDefaultGuardValue) {//default is required
      return GuardValue.USE_IF_DEFAULT;
    } else {//if (must not be default)
      return GuardValue.USE_IF_NOT_DEFAULT;
    }//
  }

  public GuardValue andGuards(List<GuardValue> guards) {
    boolean ifDefaultGuardValue = ifDefaultGuardValue(guards);
    boolean ifNotDefaultGuardValue = ifNotDefaultGuardValue(guards);
    return toDefaultFunctionUse(ifDefaultGuardValue, ifNotDefaultGuardValue);
  }

  private boolean ifNotDefaultGuardValue(List<GuardValue> guards) {
    for (GuardValue guardValue : guards) {
      switch (guardValue) {
      case USE:
      case USE_IF_NOT_DEFAULT:
        return true;
      case DO_NOT_USE:
      case USE_IF_DEFAULT:
        return false;
      }
    }
    return false;
  }

  private boolean ifDefaultGuardValue(List<GuardValue> guards) {
    for (GuardValue guardValue : guards) {
      switch (guardValue) {
      case USE:
      case USE_IF_DEFAULT:
        return true;
      case DO_NOT_USE:
      case USE_IF_NOT_DEFAULT:
        return false;
      }
    }
    return false;
  }

}
