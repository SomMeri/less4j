package com.github.sommeri.less4j.core.compiler.expressions;

import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;

public class ExpressionEvaluationInformation {
  
  private Expression original;
  private Expression evaluated;
  private IScope declarationScope; //t.j. original scope

  
}
