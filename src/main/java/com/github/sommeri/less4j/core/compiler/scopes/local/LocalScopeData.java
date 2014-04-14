package com.github.sommeri.less4j.core.compiler.scopes.local;

import java.util.HashMap;
import java.util.Map;

import com.github.sommeri.less4j.core.ast.DetachedRuleset;
import com.github.sommeri.less4j.core.compiler.scopes.FullDetachedRulesetDefinition;


public class LocalScopeData implements Cloneable {

  private VariablesDeclarationsStorage variables = new VariablesDeclarationsStorage();
  private MixinsDefinitionsStorage mixins = new MixinsDefinitionsStorage();
  private Map<DetachedRuleset, FullDetachedRulesetDefinition> detachedRulesetsScopes = new HashMap<DetachedRuleset, FullDetachedRulesetDefinition>();

  @Override
  public LocalScopeData clone() {
    try {
      LocalScopeData clone = (LocalScopeData) super.clone();
      clone.variables = variables.clone();
      clone.mixins = mixins.clone();
      clone.detachedRulesetsScopes = new HashMap<DetachedRuleset, FullDetachedRulesetDefinition>(detachedRulesetsScopes);
      return clone;

    } catch (CloneNotSupportedException e) {
      throw new IllegalStateException("Impossible to happen.");
    }
  }

  public VariablesDeclarationsStorage getVariables() {
    return variables;
  }

  public MixinsDefinitionsStorage getMixins() {
    return mixins;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder(getClass().getSimpleName()).append("\n");
    result.append("**Variables storage: ").append(variables).append("\n\n");
    result.append("**Mixins storage: ").append(mixins).append("\n\n");
    result.append("**DetachedRulesets size: ").append(detachedRulesetsScopes.size()).append("\n\n");
    return result.toString();
  }
}
