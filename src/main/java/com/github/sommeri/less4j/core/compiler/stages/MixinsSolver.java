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
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.InScopeSnapshotRunner;
import com.github.sommeri.less4j.core.compiler.scopes.InScopeSnapshotRunner.IFunction;
import com.github.sommeri.less4j.core.compiler.scopes.InScopeSnapshotRunner.ITask;
import com.github.sommeri.less4j.core.compiler.scopes.ScopeView;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.utils.ArraysUtils;

class MixinsSolver {

  private final ProblemsHandler problemsHandler;
  private final ReferencesSolver parentSolver;
  private final AstNodesStack semiCompiledNodes;

  public MixinsSolver(ReferencesSolver parentSolver, AstNodesStack semiCompiledNodes, ProblemsHandler problemsHandler) {
    this.parentSolver = parentSolver;
    this.semiCompiledNodes = semiCompiledNodes;
    this.problemsHandler = problemsHandler;
  }

  private MixinCompilationResult resolveMixinReference(final IScope callerScope, final FullMixinDefinition referencedMixin, final IScope mixinWorkingScope, final ExpressionEvaluator expressionEvaluator) {
    final ReusableStructure mixin = referencedMixin.getMixin();

    final IScope referencedMixinScope = mixinWorkingScope;
    // ... and I'm starting to see the point of closures ...
    return InScopeSnapshotRunner.runInLocalDataSnapshot(referencedMixinScope, new IFunction<MixinCompilationResult>() {

      @Override
      public MixinCompilationResult run() {
        // compile referenced mixin - keep the original copy unchanged
        List<ASTCssNode> replacement = compileReferencedMixin(mixin, referencedMixinScope);

        // collect variables and mixins to be imported
        IScope returnValues = expressionEvaluator.evaluateValues(referencedMixinScope);
        List<FullMixinDefinition> unmodifiedMixinsToImport = referencedMixinScope.getAllMixins();

        List<FullMixinDefinition> allMixinsToImport = mixinsToImport(callerScope, referencedMixinScope, unmodifiedMixinsToImport);
        returnValues.addAllMixins(allMixinsToImport);

        return new MixinCompilationResult(replacement, returnValues, unmodifiedMixinsToImport);
      }

    });
  }

  //TODO: all these methods names are too similar to each other - better and clearer naming is needed
  private List<ASTCssNode> compileReferencedMixin(ReusableStructure referencedMixin, IScope referencedMixinScopeSnapshot) {
    semiCompiledNodes.push(referencedMixin);
    try {
      GeneralBody bodyClone = referencedMixin.getBody().clone();
      parentSolver.unsafeDoSolveReferences(bodyClone, referencedMixinScopeSnapshot);
      return bodyClone.getMembers();
    } finally {
      semiCompiledNodes.pop();
    }
  }

  private List<FullMixinDefinition> mixinsToImport(IScope referenceScope, IScope referencedMixinScope, List<FullMixinDefinition> unmodifiedMixinsToImport) {
    List<FullMixinDefinition> result = new ArrayList<FullMixinDefinition>();
    for (FullMixinDefinition mixinToImport : unmodifiedMixinsToImport) {
      boolean isLocalImport = mixinToImport.getScope().seesLocalDataOf(referenceScope);
      if (isLocalImport) {
        // we need to copy the whole tree, because this runs inside referenced mixin scope 
        // snapshot and imported mixin needs to remember the scope as it is now 
        ScopeView newWay = new ScopeView(mixinToImport.getScope(), null, null);
        newWay.saveLocalDataForTheWholeWayUp();
        result.add(new FullMixinDefinition(mixinToImport.getMixin(), newWay));
      } else {
        // since this is non-local import, we need to join reference scope and imported mixins scope
        // imported mixin needs to have access to variables defined in caller
        ScopeView newWay = new ScopeView(mixinToImport.getScope(), null, referencedMixinScope);
        newWay.saveLocalDataForTheWholeWayUp();
        result.add(new FullMixinDefinition(mixinToImport.getMixin(), newWay));
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

  private IScope buildMixinsArguments(MixinReference reference, IScope referenceScope, FullMixinDefinition mixin) {
    ArgumentsBuilder builder = new ArgumentsBuilder(reference, mixin.getMixin(), new ExpressionEvaluator(referenceScope, problemsHandler), problemsHandler);
    return builder.build();
  }

  public GeneralBody buildMixinReferenceReplacement(final MixinReference reference, final IScope callerScope, List<FullMixinDefinition> mixins) {
    final GeneralBody result = new GeneralBody(reference.getUnderlyingStructure());
    for (final FullMixinDefinition fullMixin : mixins) {
      final ReusableStructure mixin = fullMixin.getMixin();
      final IScope mixinScope = fullMixin.getScope();

      // the following needs to run in snapshot because calculateMixinsWorkingScope modifies that scope
      InScopeSnapshotRunner.runInLocalDataSnapshot(mixinScope.getParent(), new ITask() {

        @Override
        public void run() {
          IScope mixinArguments = buildMixinsArguments(reference, callerScope, fullMixin);
          IScope mixinWorkingScope = calculateMixinsWorkingScope(callerScope, mixinArguments, mixinScope);

          ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(mixinWorkingScope, problemsHandler);
          if (expressionEvaluator.guardsSatisfied(mixin)) {
            MixinCompilationResult compiled = resolveMixinReference(callerScope, fullMixin, mixinWorkingScope, expressionEvaluator);

            // update mixin replacements and update scope with imported variables and mixins
            result.addMembers(compiled.getReplacement());
            callerScope.addToPlaceholder(compiled.getReturnValues());

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

  private static IScope calculateMixinsWorkingScope(IScope callerScope, IScope arguments, IScope mixinScope) {
    // add arguments
    IScope mixinDeclarationScope = mixinScope.getParent();
    mixinDeclarationScope.add(arguments);

    // locally defined mixin does not require any other action
    boolean isLocallyDefined = mixinDeclarationScope.seesLocalDataOf(callerScope);
    if (isLocallyDefined) {
      return mixinScope;
    }

    //join scopes
    IScope result = new ScopeView(mixinScope, null, callerScope);
    return result;
  }

}

class MixinCompilationResult {

  private List<ASTCssNode> replacement;
  private IScope returnValues;
  private boolean cached = false;
  private List<FullMixinDefinition> unmodifiedMixinsToImport;

  public MixinCompilationResult(List<ASTCssNode> replacement, IScope returnValues, List<FullMixinDefinition> unmodifiedMixinsToImport) {
    this.replacement = replacement;
    this.returnValues = returnValues;
    this.unmodifiedMixinsToImport = unmodifiedMixinsToImport;
  }

  public boolean wasCached() {
    return cached;
  }

  public void setCached(boolean cached) {
    this.cached = cached;
  }

  public List<ASTCssNode> getReplacement() {
    return replacement;
  }

  public void setReplacement(List<ASTCssNode> replacement) {
    this.replacement = replacement;
  }

  public IScope getReturnValues() {
    return returnValues;
  }

  public void setReturnValues(IScope returnValues) {
    this.returnValues = returnValues;
  }

  public List<FullMixinDefinition> getUnmodifiedMixinsToImport() {
    return unmodifiedMixinsToImport;
  }

  public void setUnmodifiedMixinsToImport(List<FullMixinDefinition> unmodifiedMixinsToImport) {
    this.unmodifiedMixinsToImport = unmodifiedMixinsToImport;
  }

  @Override
  protected MixinCompilationResult clone() {
    return new MixinCompilationResult(ArraysUtils.deeplyClonedList(replacement), returnValues, unmodifiedMixinsToImport);
  }

}