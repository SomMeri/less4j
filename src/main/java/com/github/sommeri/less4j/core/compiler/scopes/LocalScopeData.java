package com.github.sommeri.less4j.core.compiler.scopes;


public class LocalScopeData implements Cloneable {

  private VariablesDeclarationsStorage variables = new VariablesDeclarationsStorage();
  private MixinsDefinitionsStorage mixins = new MixinsDefinitionsStorage();

  @Override
  protected LocalScopeData clone() {
    try {
      LocalScopeData clone = (LocalScopeData) super.clone();
      clone.variables = variables.clone();
      clone.mixins = mixins.clone();
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
    return result.toString();
  }
}
