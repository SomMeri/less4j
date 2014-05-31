package com.github.sommeri.less4j.core.compiler.expressions;

import com.github.sommeri.less4j.core.compiler.scopes.FullNodeDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;

public interface LocalScopeFilter {
  
  IScope apply(IScope input);

  FullNodeDefinition apply(FullNodeDefinition value);
  
}
