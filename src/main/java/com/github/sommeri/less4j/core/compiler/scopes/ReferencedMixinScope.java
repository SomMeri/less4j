package com.github.sommeri.less4j.core.compiler.scopes;


//FIXME: premenovat a dat na rozumne miesto
public class ReferencedMixinScope {

  public static Scope joinScopes(Scope mixinsScope, Scope arguments, Scope callerScope) {
//    poradie podla dolezitosti:
//      co je definovane vnutry mixinu
//      co je v argumentoch 
//      all arguments
//      co je definovane mimo mixinu ale v jeho scope
//      co je definovane v callerovom scope
    Scope result = mixinsScope.copyWithChildChain(arguments);
    Scope mixinsScopeParent = mixinsScope.getParent();
    if (mixinsScopeParent!=null)
      arguments.setParent(mixinsScopeParent.copyWithParentsChain());
    //FIXME: arguments should join other childs of the caller scope, they should not be the SOLE child
    //FIXME: This still needs to be fixed and done right, I'm leaving it buggy in order to make the project compile
    Scope rootOfTheMixinsScope = result.getRootScope();
    rootOfTheMixinsScope.setParent(callerScope.copyWithParentsChain());
    return result;
    
  }


}
