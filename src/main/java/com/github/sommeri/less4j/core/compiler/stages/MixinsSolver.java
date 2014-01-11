package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
import com.github.sommeri.less4j.core.compiler.expressions.GuardsAndPatternsValidator;
import com.github.sommeri.less4j.core.compiler.scopes.FullMixinDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.InScopeSnapshotRunner;
import com.github.sommeri.less4j.core.compiler.scopes.InScopeSnapshotRunner.IFunction;
import com.github.sommeri.less4j.core.compiler.scopes.InScopeSnapshotRunner.ITask;
import com.github.sommeri.less4j.core.compiler.scopes.ScopeFactory;
import com.github.sommeri.less4j.core.compiler.scopes.view.ScopeView;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.utils.ArraysUtils;
import com.github.sommeri.less4j.utils.ArraysUtils.Filter;
import com.github.sommeri.less4j.utils.Counter;

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

        return new MixinCompilationResult(replacement, returnValues);
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

    final Counter wrongPatternsMixins = new Counter();
    final List<MixinCompilationResult> compiledMixins = new ArrayList<MixinCompilationResult>();

    GuardsAndPatternsValidator patternsValidator = new GuardsAndPatternsValidator(callerScope, problemsHandler);

    for (final FullMixinDefinition fullMixin : mixins) {
      final ReusableStructure mixin = fullMixin.getMixin();
      final IScope mixinScope = fullMixin.getScope();

      boolean patternsMatch = patternsValidator.patternsMatch(reference, mixin);
      if (!patternsMatch) {
        wrongPatternsMixins.increment();
      } else {
        // the following needs to run in snapshot because calculateMixinsWorkingScope modifies that scope
        InScopeSnapshotRunner.runInLocalDataSnapshot(mixinScope.getParent(), new ITask() {

          @Override
          public void run() {
            IScope mixinArguments = buildMixinsArguments(reference, callerScope, fullMixin);
            IScope mixinWorkingScope = calculateMixinsWorkingScope(callerScope, mixinArguments, mixinScope);

            GuardsAndPatternsValidator guardsValidator = new GuardsAndPatternsValidator(mixinWorkingScope, problemsHandler);
            boolean ifDefaultGuardValue = guardsValidator.guardsSatisfied(mixin, true);
            boolean ifNotDefaultGuardValue = guardsValidator.guardsSatisfied(mixin, false);

            // if none of them is true, then we do not need mixin no matter what
            if (ifDefaultGuardValue || ifNotDefaultGuardValue) {
              ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(mixinWorkingScope, problemsHandler);
              MixinCompilationResult compiled = resolveMixinReference(callerScope, fullMixin, mixinWorkingScope, expressionEvaluator);
              compiled.setDefaultFunctionUse(toDefaultFunctionUse(ifDefaultGuardValue, ifNotDefaultGuardValue));
              compiledMixins.add(compiled);
            }
          }
        });
      }
    }
    if (wrongPatternsMixins.getValue() == mixins.size()) {
      problemsHandler.patternsInMatchingMixinsDoNotMatch(reference);
    }

    // update mixin replacements and update scope with imported variables and mixins
    Set<DefaultFunctionUse> expectedUses = generateExpectedUses(compiledMixins, wrongPatternsMixins.getValue(), reference);
    for (MixinCompilationResult compiled : compiledMixins) {
      if (expectedUses.contains(compiled.getDefaultFunctionUse())) {
        result.addMembers(compiled.getReplacement());
        callerScope.addToPlaceholder(compiled.getReturnValues());
      }
    }

    callerScope.closePlaceholder();
    resolveImportance(reference, result);
    shiftComments(reference, result);

    return result;
  }

  private Set<DefaultFunctionUse> generateExpectedUses(List<MixinCompilationResult> compiledMixins, int wrongPatternsMixins, final MixinReference reference) {
    // count statistics
    int normalMixins = ArraysUtils.count(compiledMixins, DefaultFunctionUse.NONE.filter());
    int nonDefaultMixins = ArraysUtils.count(compiledMixins, DefaultFunctionUse.MUST_NOT.filter());
    int defaultMixins = ArraysUtils.count(compiledMixins, DefaultFunctionUse.REQUIRED.filter());

    //there were multiple default() mixins 
    //that is wrong in any case - guards are supposed to be constructed in such a way that this is not possibles
    if (defaultMixins > 1) {
      System.out.println("WROOOOOOOOOOOOOOOOOOOOOOOONG defaultMixins = " + defaultMixins);
      System.out.println(reference);
      return Collections.emptySet();
    }

    //there is exactly one default() mixin and nothing else matching - that is ok, we are going to use that one
    if (defaultMixins == 1 && (normalMixins == 0 && nonDefaultMixins == 0)) {
      return ArraysUtils.asSet(DefaultFunctionUse.REQUIRED);
    }

    //If there are:
    // * mixins whose guards do not use default(),
    // * or multiple not(default()) mixins,
    // * or mixin with non-matching pattern 
    if (normalMixins > 0 || nonDefaultMixins > 1) {
      //then we are going to use them and will not make fuss about anything else
      return ArraysUtils.asSet(DefaultFunctionUse.NONE, DefaultFunctionUse.MUST_NOT);
    }

    //Now we have either:
    // * zero/one default() mixin 
    // * zero/one non(default()) mixins
    // * zero normal mixins
    // * zero wrongPatternsMixins mixins
    if (nonDefaultMixins == 0 && defaultMixins == 1) {
      return ArraysUtils.asSet(DefaultFunctionUse.REQUIRED);
    }
    if (nonDefaultMixins == 1 && defaultMixins == 0) {
      if (wrongPatternsMixins == 0) {
        System.out.println("WROOOOOOOOOOOOOOOOOOOOOOOONG nonDefaultMixins = " + nonDefaultMixins);
        System.out.println(reference + " " + reference.getSourceLine());
      }
      return ArraysUtils.asSet();
    }
    if (nonDefaultMixins == 1 && defaultMixins == 1) {
      return ArraysUtils.asSet(DefaultFunctionUse.MUST_NOT);
    }
    //if (nonDefaultMixins==0 && defaultMixins==0) {
    //this should not happen - impossible state
    return Collections.emptySet();
  }

  /**
   * Re-implementing less.js heuristic. If guards value does not depend on default value, then less.js
   * assumes the default was not used. It does not check whether the default function was really used, so
   * this: not(default()), (default()) can be used multiple times.
   */
  protected DefaultFunctionUse toDefaultFunctionUse(boolean ifDefaultGuardValue, boolean ifNotDefaultGuardValue) {
    if (ifDefaultGuardValue == ifNotDefaultGuardValue) {//default was NOT used
      return DefaultFunctionUse.NONE;
    } else if (ifDefaultGuardValue) {//default is required
      return DefaultFunctionUse.REQUIRED;
    } else {//if (must not be default)
      return DefaultFunctionUse.MUST_NOT;
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

  private List<ASTCssNode> replacement;
  private IScope returnValues;
  private DefaultFunctionUse defaultFunctionUse;

  public MixinCompilationResult(List<ASTCssNode> replacement, IScope returnValues) {
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

  @Override
  protected MixinCompilationResult clone() {
    return new MixinCompilationResult(ArraysUtils.deeplyClonedList(replacement), returnValues);
  }

}

enum DefaultFunctionUse {
  NONE, REQUIRED, MUST_NOT;

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