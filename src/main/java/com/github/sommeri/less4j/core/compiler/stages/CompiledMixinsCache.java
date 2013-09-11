package com.github.sommeri.less4j.core.compiler.stages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.compiler.scopes.LocalScope.VariableRequest;
import com.github.sommeri.less4j.core.compiler.scopes.RequestCollector;
import com.github.sommeri.less4j.core.compiler.scopes.Scope;

public class CompiledMixinsCache {

  private static int counter = 0;
  private Map<CacheKey, CacheValue> cache = new HashMap<CacheKey, CacheValue>();

  public void theMixinUsedOnlyHisOwnScope(ReusableStructure mixin, Scope mixinScope, MixinCompilationResult compiled, RequestCollector requestCollector) {
//    counter++;
//    System.out.println("storing " + mixin.getNamesAsStrings() + " cache size: " + counter + " scopes copied: " + Scope.copiedScope);
//    CacheKey cacheKey = new CacheKey(mixin, mixinScope);
//    CacheValue cacheValue = cache.get(cacheKey);
//    if (cacheValue==null) {
//      cacheValue = new CacheValue();
//      cache.put(cacheKey, cacheValue);
//    }
//    //FIXME: should be self contained - have validation whether such thing already exits
//    cacheValue.add(requestCollector.getCollectedVariables(), compiled);
  }

  public MixinCompilationResult getCompiled(ReusableStructure mixin, Scope mixinScope, Scope mixinWorkingScope) {
    CacheValue result = cache.get(new CacheKey(mixin, mixinScope));
    if (result == null)
      return null;
    
    MixinCompilationResult preCompiledMixins = result.getIfMatch(mixinWorkingScope);
    if (preCompiledMixins!=null)
      System.out.println("using cached value of " + mixin.getNamesAsStrings());
    return preCompiledMixins==null? null : preCompiledMixins.clone();
  }

}

class CacheKey {

  private final ReusableStructure mixin;
  private final Scope mixinScope;

  public CacheKey(ReusableStructure mixin, Scope mixinScope) {
    this.mixin = mixin;
    this.mixinScope = mixinScope;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((mixin == null) ? 0 : mixin.hashCode());
    result = prime * result + ((mixinScope == null) ? 0 : mixinScope.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CacheKey other = (CacheKey) obj;
    if (mixin == null) {
      if (other.mixin != null)
        return false;
    } else if (!mixin.equals(other.mixin))
      return false;
    if (mixinScope == null) {
      if (other.mixinScope != null)
        return false;
    } else if (!mixinScope.equals(other.mixinScope))
      return false;
    return true;
  }

}

class CacheValue {
  
  //FIXME: (!!!) this is just a prototype, right - ok, it is not a map, it is actually a set. Does not work like map
  private Map<List<VariableRequest>, MixinCompilationResult> map = new HashMap<List<VariableRequest>, MixinCompilationResult>();

  public void add(List<VariableRequest> collectedVariables, MixinCompilationResult compiled) {
    map.put(collectedVariables, compiled);
  }

  public MixinCompilationResult getIfMatch(Scope mixinWorkingScope) {
    for (Entry<List<VariableRequest>, MixinCompilationResult> entry : map.entrySet()) {
      List<VariableRequest> requestsList = entry.getKey();
      MixinCompilationResult preCompiledMixin = entry.getValue();
      
      if (match(requestsList, mixinWorkingScope))
        return preCompiledMixin;
      
    }

    return null;
  }

  private boolean match(List<VariableRequest> requestsList, Scope mixinWorkingScope) {
    for (VariableRequest request : requestsList) {
      String name = request.getName();
      Expression lastValue = request.getValue();
      Expression currentValue = mixinWorkingScope.getVariableValueDoNotRegister(name);
      if (!match(lastValue, currentValue))
        return false;
    }
    return true;
  }

  private boolean match(Expression lastValue, Expression currentValue) {
    if (lastValue==null)
      return currentValue==null;
    
    if (!lastValue.equals(currentValue) && lastValue.toString().equals(currentValue.toString()))
      System.out.println("bwahaha");
    
    return lastValue.equals(currentValue);
  }
  
  
}
