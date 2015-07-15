package com.github.sommeri.less4j.core.compiler.stages;

import java.util.List;

import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.ScopeFactory;

/**
 * Created by Igor on 15.07.2015.
 */
public class InfiniteLoopDetector {
	private final int DEEP_OF_STACK = 10;
	private final int MIN_LOOP_COUNT = 3;
	
    public void detect(IScope scope) {
    	scope = getBodyOwnerParent(scope);
    	int loopCount = detectInfiniteLoop(scope);
    	if(loopCount >= MIN_LOOP_COUNT) 
    		throw new RuntimeException("Detected Infinite Loop: " + scope.toFullName());
    }

	private int detectInfiniteLoop(IScope scope) {
		int loopCount = 0;
    	if(scope != null){
	    	IScope preventInvocation = getPreventInvocation(scope);
	    	//hasTheSameStack
	    	boolean hasTheSameStack = hasTheSameStack(scope, preventInvocation);
	    	if(hasTheSameStack) {
	    		loopCount = detectInfiniteLoop(preventInvocation) + 1;
	    	}
    	}
		return loopCount;
	}

	private boolean hasTheSameStack(IScope s1, IScope s2) {
		int loopsCount = 0;
		boolean hasTheSameStack = false;
		do {
			if(s1 == null || s2 == null
					|| !isEqualScopes(s1, s2)){
				break;
			} else {
				s1 = getBodyOwnerParent(s1);
				s2 = getBodyOwnerParent(s2);
				loopsCount++;
			}
			hasTheSameStack = loopsCount >= DEEP_OF_STACK;
		} while (!hasTheSameStack);
		return hasTheSameStack;
	}
    
    private IScope getPreventInvocation(IScope scope){
    	if(!scope.hasParent())
    		return null;
    	IScope result = null;
    	IScope parentScope;
    	parentScope = scope;
    	do {
    		parentScope = getBodyOwnerParent(parentScope);
    		if (parentScope == null)
    			break;
    		if(isEqualScopes(scope, parentScope)){
    			result = parentScope;
    			break;
    		}
			
		} while (result == null && parentScope.hasParent());
    	return result;
    }

	private IScope getBodyOwnerParent(IScope scope) {
		if(scope.hasParent()) {
			IScope parent = scope.getParent();
			if(ScopeFactory.BODY_OWNER.equals(parent.getType())){
				return parent;
			} else {
				return getBodyOwnerParent(parent);
			}
		}
		return null;
	}

	private boolean isEqualScopes(IScope s1, IScope s2) {
		return getScopeSimpleName(s1).equals(getScopeSimpleName(s2));
	}

	private String getScopeSimpleName(IScope scope) {
		List<String> names = scope.getNames();
		if (names != null && !names.isEmpty())
			return scope.getType() + names;
		if (ScopeFactory.DEFAULT != scope.getType())
			return scope.getType() + "[" + scope.getOwner().toString() + "]";
		return scope.getType();
	}
}