package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Body;
import com.github.sommeri.less4j.core.ast.BodyOwner;
import com.github.sommeri.less4j.core.ast.Declaration;
import com.github.sommeri.less4j.core.ast.GeneralBody;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionEvaluator;
import com.github.sommeri.less4j.core.compiler.scopes.FullMixinDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.InScopeSnapshotRunner;
import com.github.sommeri.less4j.core.compiler.scopes.InScopeSnapshotRunner.ITask;
import com.github.sommeri.less4j.core.compiler.scopes.Scope;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

class MixinsSolver {

  private final ProblemsHandler problemsHandler;
  private final ReferencesSolver parentSolver;

  public MixinsSolver(ReferencesSolver parentSolver, ProblemsHandler problemsHandler) {
    this.parentSolver = parentSolver;
    this.problemsHandler = problemsHandler;
  }

  private void resolveMixinReference(final GeneralBody result, final Scope callerScope, final ReusableStructure referencedMixin, final Scope referencedMixinScope, final ExpressionEvaluator expressionEvaluator) {
    // ... and I'm starting to see the point of closures ...
    InScopeSnapshotRunner.runInLocalDataSnapshot(referencedMixinScope, new ITask() {

      @Override
      public void run() {
        unsafeResolveMixinReference(result, callerScope, referencedMixin, referencedMixinScope, expressionEvaluator);
      }

    });
  }

  private void unsafeResolveMixinReference(GeneralBody result, Scope callerScope, ReusableStructure referencedMixin, Scope referencedMixinScopeSnapshot, ExpressionEvaluator expressionEvaluator) {
    // compile referenced mixin - keep the original copy unchanged
    GeneralBody bodyClone = referencedMixin.getBody().clone();
    parentSolver.unsafeDoSolveReferences(bodyClone, referencedMixinScopeSnapshot);
    result.addMembers(bodyClone.getMembers());

    // collect variables and mixins to be imported
    Scope returnValues = expressionEvaluator.evaluateValues(referencedMixinScopeSnapshot);
    List<FullMixinDefinition> allMixinsToImport = mixinsToImport(callerScope, referencedMixin, referencedMixinScopeSnapshot);
    returnValues.addAllMixins(allMixinsToImport);
    
    // update scope with imported variables and mixins
    callerScope.addToPlaceholder(returnValues);
  }

  private List<FullMixinDefinition> mixinsToImport(Scope referenceScope, ReusableStructure referencedMixin, Scope referencedMixinScope) {
    List<FullMixinDefinition> result = new ArrayList<FullMixinDefinition>();
    for (FullMixinDefinition mixinToImport : referencedMixinScope.getAllMixins()) {
      boolean isLocalImport = mixinToImport.getScope().seesLocalDataOf(referenceScope);
      if (isLocalImport) {
        // we need to copy the whole tree, because this runs inside referenced mixin scope 
        // snapshot and imported mixin needs to remember the scope as it is now 
        Scope scopeTreeCopy = mixinToImport.getScope().copyWholeTree();
        result.add(new FullMixinDefinition(mixinToImport.getMixin(), scopeTreeCopy));
      } else {
        // since this is non-local import, we need to join reference scope and imported mixins scope
        // imported mixin would not have access to variables defined in caller
        Scope scopeTreeCopy = mixinToImport.getScope().copyWholeTree();
        scopeTreeCopy.getRootScope().setParent(referencedMixinScope.copyWithParentsChain());
        result.add(new FullMixinDefinition(mixinToImport.getMixin(), scopeTreeCopy));
      }

    }
    return result;
  }

  private void shiftComments(MixinReference reference, GeneralBody result) {
    List<ASTCssNode> childs = result.getMembers();
    if (!childs.isEmpty()) {
      childs.get(0).addOpeningComments(reference.getOpeningComments());
      childs.get(childs.size() - 1).addTrailingComments(reference.getTrailingComments());
    }
  }

  private Scope buildMixinsArguments(MixinReference reference, Scope referenceScope, FullMixinDefinition mixin) {
    ArgumentsBuilder builder = new ArgumentsBuilder(reference, mixin.getMixin(), new ExpressionEvaluator(referenceScope, problemsHandler), problemsHandler);
    return builder.build();
  }

  public GeneralBody buildMixinReferenceReplacement(final MixinReference reference, final Scope callerScope, List<FullMixinDefinition> mixins) {
    final GeneralBody result = new GeneralBody(reference.getUnderlyingStructure());
    for (final FullMixinDefinition fullMixin : mixins) {
      final ReusableStructure mixin = fullMixin.getMixin();
      final Scope mixinScope = fullMixin.getScope();
      
      // the following needs to run in snapshot because calculateMixinsWorkingScope modifies that scope
      InScopeSnapshotRunner.runInLocalDataSnapshot(mixinScope.getParent(), new ITask() {

        @Override
        public void run() {
          Scope mixinArguments = buildMixinsArguments(reference, callerScope, fullMixin);
          Scope mixinWorkingScope = calculateMixinsWorkingScope(callerScope, mixinArguments, mixinScope);

          ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(mixinWorkingScope, problemsHandler);
          if (expressionEvaluator.guardsSatisfied(mixin)) {
            resolveMixinReference(result, callerScope, fullMixin.getMixin(), mixinWorkingScope, expressionEvaluator);
          }
        }

      });

    }

    callerScope.closePlaceholder();
    resolveImportance(reference, result);
    shiftComments(reference, result);

    return result;
  }

  private void resolveImportance(MixinReference reference, GeneralBody result) {
    if (reference.isImportant()) {
      declarationsAreImportant(result);
    }
  }

  @SuppressWarnings("rawtypes")
  private void declarationsAreImportant(Body result) {
    for (ASTCssNode kid : result.getMembers()) {
      if (kid instanceof Declaration) {
        Declaration declaration = (Declaration) kid;
        declaration.setImportant(true);
      } else if (kid instanceof BodyOwner<?>) {
        BodyOwner owner = (BodyOwner) kid;
        declarationsAreImportant(owner.getBody());
      }
    }
  }

  private static Scope calculateMixinsWorkingScope(Scope callerScope, Scope arguments, Scope mixinScope) {
    // add arguments
    Scope mixinDeclarationScope = mixinScope.getParent();
    mixinDeclarationScope.add(arguments);

    // locally defined mixin does not require any other action
    boolean isLocalImport = mixinDeclarationScope.seesLocalDataOf(callerScope); 
    if (isLocalImport) {
      return mixinScope;
    }

    //join scopes
    Scope result = mixinScope.copyWholeTree();
    result.getRootScope().setParent(callerScope.copyWithParentsChain());
    return result;
  }

}
