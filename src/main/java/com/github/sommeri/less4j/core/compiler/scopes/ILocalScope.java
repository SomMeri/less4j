package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.AbstractVariableDeclaration;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.ReusableStructureName;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionFilter;
import com.github.sommeri.less4j.core.compiler.scopes.local.LocalScopeData;
import com.github.sommeri.less4j.core.compiler.scopes.local.MixinsDefinitionsStorage;
import com.github.sommeri.less4j.core.compiler.scopes.local.MixinsDefinitionsStorage.MixinsPlaceholder;
import com.github.sommeri.less4j.core.compiler.scopes.local.VariablesDeclarationsStorage;
import com.github.sommeri.less4j.core.compiler.scopes.local.VariablesDeclarationsStorage.VariablesPlaceholder;

public interface ILocalScope {

  // names
  public void addNames(List<String> names);

  public List<String> getNames();
  
  // scope description
  public ASTCssNode getOwner();

  public String getType();

  public boolean isBodyOwnerScope();

  public boolean hasTheSameLocalData(ILocalScope otherScope);

  public boolean isPresentInAst();

  public void removedFromAst();

  // placeholders for data from references
  public DataPlaceholder createDataPlaceholder(); 

  public void addToDataPlaceholder(IScope otherScope);

  public void replacePlaceholder(DataPlaceholder placeholder, IScope otherScope);

  public void closeDataPlaceholder();

  // snapshots for temporary evalution that does not require tree change
  public void createCurrentDataSnapshot();

  public void createOriginalDataSnapshot();

  public void discardLastDataSnapshot();

  public ILocalScope cloneCurrentDataSnapshot();

  // variables
  public void registerVariable(AbstractVariableDeclaration declaration);

  public void registerVariable(AbstractVariableDeclaration node, Expression replacementValue);

  public void registerVariableIfNotPresent(String name, Expression replacementValue);

  public void registerVariable(String name, Expression replacementValue);

  public void addFilteredVariables(ExpressionFilter filter, IScope source);

  public Expression getValue(Variable variable);

  public Expression getValue(String name);

  // mixins
  public void registerMixin(ReusableStructure mixin, IScope mixinsBodyScope);

  public void addAllMixins(List<FullMixinDefinition> mixins);

  public List<FullMixinDefinition> getAllMixins();

  public List<FullMixinDefinition> getMixinsByName(List<String> nameChain, ReusableStructureName name);

  public List<FullMixinDefinition> getMixinsByName(ReusableStructureName name);

  public List<FullMixinDefinition> getMixinsByName(String name);
  
  // other scopes and internals - these could be removed with proper refactoring. It would be technically cleaner, but it does not seem to be too important rigth now
  public void add(IScope otherSope);

  public MixinsDefinitionsStorage getLocalMixins();

  public VariablesDeclarationsStorage getLocalVariables();

  public LocalScopeData getLocalData();

  public static final class DataPlaceholder {

    private VariablesPlaceholder variablesPlaceholder;
    private MixinsPlaceholder mixinsPlaceholder;

    public DataPlaceholder(VariablesPlaceholder variablesPlaceholder, MixinsPlaceholder mixinsPlaceholder) {
      this.variablesPlaceholder = variablesPlaceholder;
      this.mixinsPlaceholder = mixinsPlaceholder;
    }

    public VariablesPlaceholder getVariablesPlaceholder() {
      return variablesPlaceholder;
    }

    public MixinsPlaceholder getMixinsPlaceholder() {
      return mixinsPlaceholder;
    }
    
  }
}
