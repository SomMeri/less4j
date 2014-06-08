package com.github.sommeri.less4j.core.ast;

import com.github.sommeri.less4j.core.compiler.scopes.IScope;

public interface IScopeAware {
	
	IScope getScope();

	void setScope(IScope scope);

	boolean hasScope();
	
}
