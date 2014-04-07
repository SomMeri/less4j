package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.GeneralBody;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.ReusableStructureName;
import com.github.sommeri.less4j.core.compiler.scopes.FullMixinDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.InScopeSnapshotRunner;
import com.github.sommeri.less4j.core.compiler.scopes.InScopeSnapshotRunner.ITask;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;

public class MixinReferenceFinder {

  private final ReferencesSolver parentSolver;
  private final AstNodesStack semiCompiledNodes;
  private boolean foundNamespace = false;

  public MixinReferenceFinder(ReferencesSolver referencesSolver, AstNodesStack semiCompiledNodes) {
    this.parentSolver = referencesSolver;
    this.semiCompiledNodes = semiCompiledNodes;
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
  public List<FullMixinDefinition> getNearestMixins(IScope scope, MixinReference reference) {
    foundNamespace = false;
    List<String> nameChain = reference.getNameChainAsStrings();
    IScope space = scope;

    List<FullMixinDefinition> result = findInMatchingNamespace(scope, nameChain, reference);
    while (result.isEmpty() && space.hasParent()) {
      space = space.getParent();
      result = findInMatchingNamespace(space, nameChain, reference);
    }
    return result;
  }

  public boolean foundNamespace() {
    return foundNamespace;
  }

  private List<FullMixinDefinition> getNearestLocalMixins(IScope scope, List<String> nameChain, ReusableStructureName name) {
    if (scope.isBodyOwnerScope())
      scope = scope.firstChild();

    List<FullMixinDefinition> mixins = scope.getMixinsByName(nameChain, name);
    if (mixins != null)
      mixins = removeLegalCycles(mixins, name);

    return mixins == null ? new ArrayList<FullMixinDefinition>() : mixins;
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

  private List<FullMixinDefinition> findInMatchingNamespace(IScope scope, List<String> nameChain, MixinReference reference) {
    List<FullMixinDefinition> result = new ArrayList<FullMixinDefinition>();

    if (nameChain.isEmpty()) {
      foundNamespace = true;
    } else {
      for (int prefix = 1; prefix <= nameChain.size(); prefix++) {
        String name = toName(nameChain.subList(0, prefix));
        List<String> theRest = prefix==nameChain.size()? new ArrayList<String>(): nameChain.subList(prefix, nameChain.size());

        for (FullMixinDefinition fullMixin : scope.getMixinsByName(name)) {
          List<FullMixinDefinition> foundInNamespaces = buildAndFind(fullMixin, theRest, reference);
          result.addAll(foundInNamespaces);
        }
      }
//      String firstName = nameChain.get(0);
//      List<String> theRest = nameChain.subList(1, nameChain.size());
//
//      for (FullMixinDefinition fullMixin : scope.getMixinsByName(firstName)) {
//        List<FullMixinDefinition> foundInNamespaces = buildAndFind(fullMixin, theRest, reference);
//        result.addAll(foundInNamespaces);
//      }
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

  private List<FullMixinDefinition> buildAndFind(FullMixinDefinition fullMixin, final List<String> nameChain, final MixinReference reference) {

    final List<FullMixinDefinition> result = new ArrayList<FullMixinDefinition>();

    final ReusableStructure mixin = fullMixin.getMixin();
    final GeneralBody bodyClone = mixin.getBody().clone();
    final IScope scope = fullMixin.getScope();

    InScopeSnapshotRunner.runInLocalDataSnapshot(scope, new ITask() {

      @Override
      public void run() {
        // Referenced namespace attempts to import its own mixins. Do not
        // try to compile it.
        if (!semiCompiledNodes.contains(bodyClone)) {
          parentSolver.unsafeDoSolveReferences(bodyClone, scope);
        }

        List<FullMixinDefinition> found = findInMatchingNamespace(scope, nameChain, reference);
        result.addAll(found);
      }

    });

    return result;
  }

}
