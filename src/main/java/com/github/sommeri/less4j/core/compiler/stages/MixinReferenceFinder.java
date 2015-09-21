package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.LessCompiler.Configuration;
import com.github.sommeri.less4j.core.ast.GeneralBody;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.ReusableStructureName;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionEvaluator;
import com.github.sommeri.less4j.core.compiler.expressions.GuardValue;
import com.github.sommeri.less4j.core.compiler.expressions.MixinsGuardsValidator;
import com.github.sommeri.less4j.core.compiler.scopes.FoundMixin;
import com.github.sommeri.less4j.core.compiler.scopes.FullMixinDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.InScopeSnapshotRunner;
import com.github.sommeri.less4j.core.compiler.scopes.InScopeSnapshotRunner.ITask;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class MixinReferenceFinder {

  private final ReferencesSolver parentSolver;
  private final AstNodesStack semiCompiledNodes;
  private boolean foundNamespace = false;
  private final ProblemsHandler problemsHandler;
  private final Configuration configuration;

  public MixinReferenceFinder(ReferencesSolver referencesSolver, AstNodesStack semiCompiledNodes, ProblemsHandler problemsHandler, Configuration configuration) {
    this.parentSolver = referencesSolver;
    this.semiCompiledNodes = semiCompiledNodes;
    this.problemsHandler = problemsHandler;
    this.configuration = configuration;
  }

  /**
   * Find referenced mixins by name. It does not check number of parameters nor
   * guards, because the search should stop once any mixin with referenced name
   * is found.
   * 
   * However, it checks every encountered mixin for "legal" cycle (e.g. ruleset
   * needing itself) and completely ignores those who would cycle that way.
   * 
   */
  public List<FoundMixin> getNearestMixins(IScope scope, MixinReference reference) {
    foundNamespace = false;
    List<String> nameChain = reference.getNameChainAsStrings();
    IScope space = scope;

    List<FoundMixin> result = findInMatchingNamespace(scope, nameChain, reference);
    while (result.isEmpty() && space.hasParent()) {
      space = space.getParent();
      result = findInMatchingNamespace(space, nameChain, reference);
    }
    return result;
  }

  public boolean foundNamespace() {
    return foundNamespace;
  }

  private List<FoundMixin> getNearestLocalMixins(IScope scope, List<String> nameChain, ReusableStructureName name) {
    if (scope.isBodyOwnerScope())
      scope = scope.firstChild();

    List<FullMixinDefinition> mixins = scope.getMixinsByName(nameChain, name);
    if (mixins != null)
      mixins = removeLegalCycles(mixins, name);
    
    List<FoundMixin> result = new ArrayList<FoundMixin>();
    if (mixins == null)
      return result;

    for (FullMixinDefinition fulDefinition : mixins) {
      result.add(new FoundMixin(fulDefinition));
    }
    return  result;
  }

  private List<FullMixinDefinition> removeLegalCycles(List<FullMixinDefinition> value, ReusableStructureName name) {
    List<FullMixinDefinition> result = new ArrayList<FullMixinDefinition>();
    for (FullMixinDefinition mixin : value) {
      if (!participatesInCuttableCycle(mixin))
        result.add(mixin);
    }
    return result;
  }

  private boolean participatesInCuttableCycle(FullMixinDefinition mixin) {
    /*
     * Detecting following kind of cycle:
     * 
     * .simpleCase { .simpleCase(); }
     * 
     * Note: the detection feels too hackish, but works and hotfix is needed
     * (bootstrap).
     */
    return mixin.getMixin().isAlsoRuleset() && semiCompiledNodes.contains(mixin.getMixin());
  }

  private List<FoundMixin> findInMatchingNamespace(IScope scope, List<String> nameChain, MixinReference reference) {
    List<FoundMixin> result = new ArrayList<FoundMixin>();

    if (nameChain.isEmpty()) {
      foundNamespace = true;
    } else {
      for (int prefix = 1; prefix <= nameChain.size(); prefix++) {
        String name = toName(nameChain.subList(0, prefix));
        List<String> theRest = prefix==nameChain.size()? new ArrayList<String>(): nameChain.subList(prefix, nameChain.size());

        for (FullMixinDefinition fullNamespace : scope.getMixinsByName(name)) {
          List<FoundMixin> foundInNamespaces = buildAndFind(fullNamespace, theRest, reference);
          result.addAll(foundInNamespaces);
        }
      }
    }

    result.addAll(getNearestLocalMixins(scope, nameChain, reference.getFinalName()));
    return result;
  }

  private String toName(List<String> list) {
    StringBuilder builder = new StringBuilder();
    for (String string : list) {
      builder.append(string);
    }
    return builder.toString();
  }

  private List<FoundMixin> buildAndFind(FullMixinDefinition fullNamespace, final List<String> nameChain, final MixinReference reference) {

    final List<FoundMixin> result = new ArrayList<FoundMixin>();

    final ReusableStructure namespace = fullNamespace.getMixin();
    final GeneralBody bodyClone = namespace.getBody().clone();
    final IScope scope = fullNamespace.getScope();
    
    if (namespace.hasMandatoryParameters()) {
      return result;
    }

    InScopeSnapshotRunner.runInLocalDataSnapshot(scope, new ITask() {

      @Override
      public void run() {
        // Referenced namespace attempts to import its own mixins. Do not
        // try to compile it.
        if (!semiCompiledNodes.contains(bodyClone)) {
          parentSolver.unsafeDoSolveReferences(bodyClone, scope);
         
          // this needs to be done to enforce https://github.com/SomMeri/less4j/issues/163
          ExpressionEvaluator evaluator = new ExpressionEvaluator(scope, problemsHandler, configuration);
          evaluator.evaluateValues(scope);
        }
        MixinsGuardsValidator guardsValidator = new MixinsGuardsValidator(scope, problemsHandler, configuration);
        GuardValue guardValue = guardsValidator.evaluateGuards(namespace);
        
        if (guardValue!=GuardValue.DO_NOT_USE) { //FIXME: maybe, maybe not, I am not sure now
          List<FoundMixin> found = findInMatchingNamespace(scope, nameChain, reference);
          for (FoundMixin foundMixin : found) {
            foundMixin.prefixGuardValue(guardValue);
            result.add(foundMixin);
          }
        }
        
      }

    });

    return result;
  }

}
