package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Body;
import com.github.sommeri.less4j.core.ast.BodyOwner;
import com.github.sommeri.less4j.core.ast.Declaration;
import com.github.sommeri.less4j.core.ast.GeneralBody;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionEvaluator;
import com.github.sommeri.less4j.core.compiler.expressions.GuardsValidator;
import com.github.sommeri.less4j.core.compiler.scopes.FullMixinDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.InScopeSnapshotRunner;
import com.github.sommeri.less4j.core.compiler.scopes.InScopeSnapshotRunner.IFunction;
import com.github.sommeri.less4j.core.compiler.scopes.InScopeSnapshotRunner.ITask;
import com.github.sommeri.less4j.core.compiler.scopes.ScopeFactory;
import com.github.sommeri.less4j.core.compiler.scopes.view.ScopeView;
import com.github.sommeri.less4j.core.problems.BugHappened;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.utils.ArraysUtils;
import com.github.sommeri.less4j.utils.ArraysUtils.Filter;

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
    return InScopeSnapshotRunner.runInOriginalDataSnapshot(referencedMixinScope, new IFunction<MixinCompilationResult>() {

      @Override
      public MixinCompilationResult run() {
        // compile referenced mixin - keep the original copy unchanged
        List<ASTCssNode> replacement = compileReferencedMixin(mixin, referencedMixinScope);

        // collect variables and mixins to be imported
        IScope returnValues = expressionEvaluator.evaluateValues(referencedMixinScope);
        List<FullMixinDefinition> unmodifiedMixinsToImport = referencedMixinScope.getAllMixins();

        List<FullMixinDefinition> allMixinsToImport = mixinsToImport(callerScope, referencedMixinScope, unmodifiedMixinsToImport);
        returnValues.addAllMixins(allMixinsToImport);

        return new MixinCompilationResult(mixin, replacement, returnValues);
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
        ScopeView newWay = ScopeFactory.createJoinedScopesView(null, mixinToImport.getScope());
        newWay.saveLocalDataForTheWholeWayUp();
        result.add(new FullMixinDefinition(mixinToImport.getMixin(), newWay));
      } else {
        // since this is non-local import, we need to join reference scope and imported mixins scope
        // imported mixin needs to have access to variables defined in caller
        ScopeView newWay = ScopeFactory.createJoinedScopesView(referencedMixinScope, mixinToImport.getScope());
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
    GeneralBody result = new GeneralBody(reference.getUnderlyingStructure());
    if (mixins.isEmpty())
      return result;

    //candidate mixins with information about their default() function use are stored here
    final List<MixinCompilationResult> compiledMixins = new ArrayList<MixinCompilationResult>();

    for (final FullMixinDefinition fullMixin : mixins) {
      final ReusableStructure mixin = fullMixin.getMixin();
      final IScope mixinScope = fullMixin.getScope();

      // the following needs to run in snapshot because calculateMixinsWorkingScope modifies that scope
      InScopeSnapshotRunner.runInLocalDataSnapshot(mixinScope.getParent(), new ITask() {

        @Override
        public void run() {
          IScope mixinArguments = buildMixinsArguments(reference, callerScope, fullMixin);
          IScope mixinWorkingScope = calculateMixinsWorkingScope(callerScope, mixinArguments, mixinScope);

          GuardsValidator guardsValidator = new GuardsValidator(mixinWorkingScope, problemsHandler);
          boolean ifDefaultGuardValue = guardsValidator.guardsSatisfied(mixin, true);
          boolean ifNotDefaultGuardValue = guardsValidator.guardsSatisfied(mixin, false);

          // if none of them is true, then we do not need mixin no matter what
          if (ifDefaultGuardValue || ifNotDefaultGuardValue) {
            //OPTIMIZATION POSSIBLE: there is no need to compile mixins at this point, some of them are not going to be 
            //used and create snapshot operation is cheap now. It should be done later on.
            ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(mixinWorkingScope, problemsHandler);
            MixinCompilationResult compiled = resolveMixinReference(callerScope, fullMixin, mixinWorkingScope, expressionEvaluator);
            //mark the mixin according to its default() function use 
            compiled.setDefaultFunctionUse(toDefaultFunctionUse(ifDefaultGuardValue, ifNotDefaultGuardValue));
            //store the mixin as candidate
            compiledMixins.add(compiled);
          }
        }
      });
    }

    // filter out mixins we do not want to use  
    List<MixinCompilationResult> mixinsToBeUsed = chooseMixinsToBeUsed(compiledMixins, reference);
    
    // update mixin replacements and update scope with imported variables and mixins
    for (MixinCompilationResult compiled : mixinsToBeUsed) {
      result.addMembers(compiled.getReplacement());
      callerScope.addToPlaceholder(compiled.getReturnValues());
    }

    callerScope.closePlaceholder();
    resolveImportance(reference, result);
    shiftComments(reference, result);

    return result;
  }

  private List<MixinCompilationResult> chooseMixinsToBeUsed(List<MixinCompilationResult> compiledMixins, final MixinReference reference) {
    // count how many mixins of each kind we encountered
    int normalMixinsCnt = ArraysUtils.count(compiledMixins, DefaultFunctionUse.DEFAULT_OBLIVIOUS.filter());
    int ifNotCnt = ArraysUtils.count(compiledMixins, DefaultFunctionUse.ONLY_IF_NOT_DEFAULT.filter());
    int ifDefaultCnt = ArraysUtils.count(compiledMixins, DefaultFunctionUse.ONLY_IF_DEFAULT.filter());
    
    //sanity check - could be removed - keeping only for debugging purposes
    if (normalMixinsCnt+ifNotCnt+ifDefaultCnt!=compiledMixins.size())
      throw new BugHappened("Unexpected mixin type in compiled mixins list.", reference);

    //there were multiple default() mixins 
    //that is wrong in any case - guards are supposed to be constructed in such a way that this is not possibles
    if (ifDefaultCnt > 1) {
      List<MixinCompilationResult> errorSet = keepOnly(compiledMixins, DefaultFunctionUse.ONLY_IF_DEFAULT);
      problemsHandler.ambiguousDefaultSet(reference, extractOriginalMixins(errorSet));
      //no mixins are going to be used
      return Collections.emptyList();
    }
    //there are  multiple not(default()) mixins and nothing else - that is considered to be ambiuous too
    if (ifDefaultCnt == 0 && normalMixinsCnt==0 && ifNotCnt > 1) {
      List<MixinCompilationResult> errorSet = keepOnly(compiledMixins, DefaultFunctionUse.ONLY_IF_NOT_DEFAULT);
      problemsHandler.ambiguousNotDefaultSet(reference, extractOriginalMixins(errorSet));
      //no mixins are going to be used
      return Collections.emptyList();
    }

    //there is exactly one default() mixin and nothing else - that is ok, we are going to use that one
    if (ifDefaultCnt == 1 && normalMixinsCnt == 0 && ifNotCnt == 0) {
      return compiledMixins; 
    }

    //If there is:
    // * at least one mixin whose guards do not use default(),
    // * or multiple not(default()) mixins,
    if (normalMixinsCnt > 0 || ifNotCnt > 1) {
      //then we are going to use only them and ignore defaults 
      return keepOnly(compiledMixins, DefaultFunctionUse.DEFAULT_OBLIVIOUS, DefaultFunctionUse.ONLY_IF_NOT_DEFAULT);
    }

    //Now:
    //* if there is default mixin, then there is something else with it too.
    //* there are no normal mixins and there is maximum one not(default()) mixin
    
    //Therefore, we have either:
    // * exactly one default() mixin and one non(default()) mixin
    // * exactly one non(default()) mixins
    // * nothing

    //if there are both of them, use only non(default())
    if (ifNotCnt == 1 && ifDefaultCnt == 1) {
      return keepOnly(compiledMixins, DefaultFunctionUse.ONLY_IF_NOT_DEFAULT);
    }
    //if there is only non(default()), then we can not use it
    return Collections.emptyList();
  }

  /**
   * Removes all comiled mixins from compiledMixins list with wrong use of default function. 
   * Warning: Modifies the compiledMixins list.

   * @param compiledMixins - list of compiled mixins - will be modified. 
   * @param kind - types of mixins that are going to stay.
   * @return compiledMixins - for convenience
   */
  private List<MixinCompilationResult> keepOnly(List<MixinCompilationResult> compiledMixins, DefaultFunctionUse... kind) {
    Set<DefaultFunctionUse> expectedUses = ArraysUtils.asSet(kind);
    Iterator<MixinCompilationResult> iterator = compiledMixins.iterator();
    while (iterator.hasNext()) {
      MixinCompilationResult compiled = iterator.next();
      if (!expectedUses.contains(compiled.getDefaultFunctionUse())) {
        iterator.remove();
      }
    }
    return compiledMixins;
  }

  private List<ReusableStructure> extractOriginalMixins(List<MixinCompilationResult> compiledMixins) {
    List<ReusableStructure> result = new ArrayList<ReusableStructure>();
    for (MixinCompilationResult compiled : compiledMixins) {
      result.add(compiled.getMixin());
    }
    return result;
  }

  /**
   * Re-implementing less.js heuristic. If guards value does not depend on default value, then less.js
   * assumes the default was not used. It does not check whether the default function was really used, so
   * this: not(default()), (default()) can be used multiple times.
   */
  protected DefaultFunctionUse toDefaultFunctionUse(boolean ifDefaultGuardValue, boolean ifNotDefaultGuardValue) {
    if (ifDefaultGuardValue == ifNotDefaultGuardValue) {//default was NOT used
      return DefaultFunctionUse.DEFAULT_OBLIVIOUS;
    } else if (ifDefaultGuardValue) {//default is required
      return DefaultFunctionUse.ONLY_IF_DEFAULT;
    } else {//if (must not be default)
      return DefaultFunctionUse.ONLY_IF_NOT_DEFAULT;
    }//
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
    IScope result = ScopeFactory.createJoinedScopesView(callerScope, mixinScope);
    return result;
  }

}

class MixinCompilationResult {

  private ReusableStructure mixin;
  private List<ASTCssNode> replacement;
  private IScope returnValues;
  private DefaultFunctionUse defaultFunctionUse;

  public MixinCompilationResult(ReusableStructure mixin, List<ASTCssNode> replacement, IScope returnValues) {
    this.mixin = mixin;
    this.replacement = replacement;
    this.returnValues = returnValues;
  }

  public void setDefaultFunctionUse(DefaultFunctionUse defaultFunctionUse) {
    this.defaultFunctionUse = defaultFunctionUse;
  }

  public DefaultFunctionUse getDefaultFunctionUse() {
    return defaultFunctionUse;
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

  public ReusableStructure getMixin() {
    return mixin;
  }

  public void setMixin(ReusableStructure mixin) {
    this.mixin = mixin;
  }

  @Override
  protected MixinCompilationResult clone() {
    return new MixinCompilationResult(mixin.clone(), ArraysUtils.deeplyClonedList(replacement), returnValues);
  }

}

enum DefaultFunctionUse {
  DEFAULT_OBLIVIOUS, ONLY_IF_DEFAULT, ONLY_IF_NOT_DEFAULT;

  public Filter<MixinCompilationResult> filter() {
    return new DefaultFunctionUseFilter(this);
  }
}

class DefaultFunctionUseFilter implements Filter<MixinCompilationResult> {

  private final DefaultFunctionUse value;

  public DefaultFunctionUseFilter(DefaultFunctionUse value) {
    super();
    this.value = value;
  }

  @Override
  public boolean accept(MixinCompilationResult t) {
    return t.getDefaultFunctionUse().equals(value);
  }

}