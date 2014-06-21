package com.github.sommeri.less4j.core.ast;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;

/**
 * Contains scope associated with the node. It would be much more cleaner if 
 * the solution would keep ast completely independent from scope. That being said,
 * I did not found such clean solution that would not be also fragile or too 
 * complicated or required too much boiler plate like code. 
 *
 */
public interface IScopeAware {
	
  @NotAstProperty
	IScope getScope();

  @NotAstProperty
	void setScope(IScope scope);

  @NotAstProperty
	boolean hasScope();
	
}
