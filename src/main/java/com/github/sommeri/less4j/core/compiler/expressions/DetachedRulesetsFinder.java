package com.github.sommeri.less4j.core.compiler.expressions;

import com.github.sommeri.less4j.LessCompiler.Configuration;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class DetachedRulesetsFinder extends ExpressionEvaluator {

  public DetachedRulesetsFinder(IScope scope, ProblemsHandler problemsHandler, Configuration configuration) {
    super(scope, problemsHandler, configuration);
  }
  
  
}
