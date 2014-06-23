package com.github.sommeri.less4j.core.compiler.expressions;

import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;

@Deprecated //FIXME: !!!!!!!!!!!!!!!! delete interface
public interface LocalScopeFilter {
  
  IScope apply(IScope input);

  Expression apply(Expression value);
  
}
