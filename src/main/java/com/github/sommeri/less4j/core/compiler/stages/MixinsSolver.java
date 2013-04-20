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
import com.github.sommeri.less4j.core.compiler.scopes.Scope;
import com.github.sommeri.less4j.core.compiler.scopes.InScopeSnapshotRunner.ITask;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

class MixinsSolver {

  private final ProblemsHandler problemsHandler;
  private final ReferencesSolver parentSolver;

  public MixinsSolver(ReferencesSolver parentSolver, ProblemsHandler problemsHandler) {
    this.parentSolver = parentSolver;
    this.problemsHandler = problemsHandler;
  }

  private void resolveMixinReferenceInSnapshot(final Scope referenceScope, final GeneralBody result, final ReusableStructure referencedMixin, final Scope referencedMixinScope, final ExpressionEvaluator expressionEvaluator) {
    // ... and I'm starting to see the point of closures ...
    InScopeSnapshotRunner.runInLocalDataSnapshot(referencedMixinScope, new ITask() {

      @Override
      public void run() {
        resolveMixinReference(referenceScope, result, referencedMixin, referencedMixinScope, expressionEvaluator);
      }

    });
  }

  private void resolveMixinReference(Scope referenceScope, GeneralBody result, ReusableStructure referencedMixin, Scope referencedMixinScope, ExpressionEvaluator expressionEvaluator) {
    GeneralBody bodyClone = referencedMixin.getBody().clone();
    parentSolver.doSolveReferences(bodyClone, referencedMixinScope);
    result.addMembers(bodyClone.getMembers());

    List<FullMixinDefinition> allMixinsToImport = new ArrayList<FullMixinDefinition>();
    for (FullMixinDefinition mixinToImport : referencedMixinScope.getAllMixins()) {
      // FIXME: !!!! add unit test imports chain: local import, outside import
      // FIXME: !!!! robit kopie len ak to naozaj treba
      Scope scopeTreeCopy = mixinToImport.getScope().copyWholeTree();
      if (AstLogic.canHaveArguments(referencedMixin)) {
        Scope argumentsAwaitingParent = scopeTreeCopy.findFirstArgumentsAwaitingParent();
        // Mixin can be imported either from inside or outside. If it is from inside, nothing is needed. If it is from 
        // outside, then we need to join scopes. 
        if (argumentsAwaitingParent == null) { // FIXME: !!!! this is a bad way of checking inside vs outside import
          scopeTreeCopy.getRootScope().setParent(referencedMixinScope.copyWithParentsChain());
        }
      }

      allMixinsToImport.add(new FullMixinDefinition(mixinToImport.getMixin(), scopeTreeCopy));
    }

    // update scope
    Scope returnValues = expressionEvaluator.evaluateValues(referencedMixinScope);
    returnValues.addAllMixins(allMixinsToImport);
    referenceScope.addToPlaceholder(returnValues);
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

  // FIXME: !!!! the ugliest code ever seen, refactor it before commit 
  public GeneralBody resolveReferencedMixins(final MixinReference reference, final Scope referenceScope, List<FullMixinDefinition> mixins) {
    final GeneralBody result = new GeneralBody(reference.getUnderlyingStructure());
    for (final FullMixinDefinition fullReferencedMixin : mixins) {
      final ReusableStructure referencedMixin = fullReferencedMixin.getMixin();
      final Scope referencedScope = fullReferencedMixin.getScope();

      InScopeSnapshotRunner.runInLocalDataSnapshot(referencedScope.getParent(), new ITask() {

        @Override
        public void run() {
          Scope mixinArguments = buildMixinsArguments(reference, referenceScope, fullReferencedMixin);
          Scope referencedMixinScope = calculateMixinsWorkingScope(referenceScope, mixinArguments, referencedScope);

          ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(referencedMixinScope, problemsHandler);

          if (expressionEvaluator.guardsSatisfied(referencedMixin)) {
            resolveMixinReferenceInSnapshot(referenceScope, result, fullReferencedMixin.getMixin(), referencedMixinScope, expressionEvaluator);
          }
        }

      });

    }

    referenceScope.closePlaceholder();
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
    mixinDeclarationScope.addAsArguments(arguments);

    // locally defined mixin does not require any other action
    if (mixinDeclarationScope.getParent() == callerScope) {
      return mixinScope;
    }

    //join scopes
    Scope result = mixinScope.copyWholeTree();
    result.getRootScope().setParent(callerScope.copyWithParentsChain());
    return result;
  }

}
