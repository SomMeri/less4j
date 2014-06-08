package com.github.sommeri.less4j.core.compiler.expressions;

import com.github.sommeri.less4j.core.compiler.scopes.FullExpressionDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;

public interface LocalScopeFilter {
  
  IScope apply(IScope input);

  FullExpressionDefinition apply(FullExpressionDefinition value);
  
}
