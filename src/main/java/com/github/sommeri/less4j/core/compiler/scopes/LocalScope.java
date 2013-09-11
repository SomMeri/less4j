package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.ReusableStructureName;

public class LocalScope {

  //FIXME: (!!!) does this needs to be protected?
  protected final ASTCssNode owner;
  private LocalScopeData localData = new LocalScopeData();
  private Stack<LocalScopeData> localDataSnapshots = new Stack<LocalScopeData>();

  public LocalScope(ASTCssNode owner) {
    this.owner = owner;
  }

  public LocalScope(ASTCssNode owner, LocalScopeData initialLocalData) {
    this(owner);
    localData =  initialLocalData;
  }

  protected LocalScopeData getLocalData() {
    return localData;
  }

  public boolean hasTheSameLocalData(LocalScope otherScope) {
    return otherScope.localData == localData;
  }

  /**
   * Do not call this method directly. Use {@link InScopeSnapshotRunner}
   * instead.
   */
  protected void createLocalDataSnapshot() {
    localDataSnapshots.push(localData);
    localData = localData.clone();
  }

  /**
   * Do not call this method directly. Use {@link InScopeSnapshotRunner}
   * instead.
   */
  protected void discardLastLocalDataSnapshot() {
    localData = localDataSnapshots.pop();
  }

  protected MixinsDefinitionsStorage getLocalMixins() {
    return localData.mixins;
  }

  protected VariablesDeclarationsStorage getLocalVariables() {
    return localData.variables;
  }

  public List<FullMixinDefinition> getAllMixins() {
    registerMixinRequest("*");
    return getLocalMixins().getAllMixins();
  }

  public List<FullMixinDefinition> getMixinsByName(List<String> nameChain, ReusableStructureName name) {
    registerMixinRequest("... > " + name);
    return getLocalMixins().getMixins(nameChain, name);
  }

  public List<FullMixinDefinition> getMixinsByName(ReusableStructureName name) {
    registerMixinRequest(name.asString());
    return getLocalMixins().getMixins(name);
  }

  public List<FullMixinDefinition> getMixinsByName(String name) {
    registerMixinRequest(name);
    return getLocalMixins().getMixins(name);
  }

  @Override
  public String toString() {
    return localData.toString();
  }

  protected class LocalScopeData implements Cloneable {

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

  private List<RequestCollector> collectors = new ArrayList<RequestCollector>();

  public void addRequestCollector(RequestCollector requestCollector) {
    collectors.add(requestCollector);
  }

  public void removeRequestCollector(RequestCollector requestCollector) {
    collectors.remove(requestCollector);
  }

  //FIXME: (!!!) this is a hack!!! - should be private or removed
  protected void registerVariableRequest(String name, Expression value) {
    for (RequestCollector col : collectors) {
      col.add(new VariableRequest(name, value));
    }
  }

  private void registerMixinRequest(String name) {
    for (RequestCollector col : collectors) {
      col.wasMixinRequest(name);
    }
  }

  public class VariableRequest {

    private final String name;
    private final Expression value;

    public VariableRequest(String name, Expression value) {
      this.name = name;
      this.value = value;
    }

    public String getName() {
      return name;
    }

    public Expression getValue() {
      return value;
    }

    @Override
    public String toString() {
      return "VariableRequest [" + name + ": " + value + "]";
    }

  }

}
