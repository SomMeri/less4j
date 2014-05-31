package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.LessCompiler.Configuration;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Body;
import com.github.sommeri.less4j.core.ast.BodyOwner;
import com.github.sommeri.less4j.core.ast.Declaration;
import com.github.sommeri.less4j.core.ast.DetachedRuleset;
import com.github.sommeri.less4j.core.ast.DetachedRulesetReference;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.GeneralBody;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionEvaluator;
import com.github.sommeri.less4j.core.compiler.expressions.GuardValue;
import com.github.sommeri.less4j.core.compiler.expressions.LocalScopeFilter;
import com.github.sommeri.less4j.core.compiler.expressions.MixinsGuardsValidator;
import com.github.sommeri.less4j.core.compiler.scopes.FullMixinDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.FullNodeDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.InScopeSnapshotRunner;
import com.github.sommeri.less4j.core.compiler.scopes.InScopeSnapshotRunner.IFunction;
import com.github.sommeri.less4j.core.compiler.scopes.InScopeSnapshotRunner.ITask;
import com.github.sommeri.less4j.core.compiler.scopes.ScopeFactory;
import com.github.sommeri.less4j.core.compiler.scopes.view.ScopeView;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

class MixinsSolver {

  private final ProblemsHandler problemsHandler;
  private final ReferencesSolver parentSolver;
  private final AstNodesStack semiCompiledNodes;
  private final Configuration configuration;
  private final DefaultGuardHelper defaultGuardHelper;

  public MixinsSolver(ReferencesSolver parentSolver, AstNodesStack semiCompiledNodes, ProblemsHandler problemsHandler, Configuration configuration) {
    this.parentSolver = parentSolver;
    this.semiCompiledNodes = semiCompiledNodes;
    this.problemsHandler = problemsHandler;
    this.configuration = configuration;
    this.defaultGuardHelper = new DefaultGuardHelper(problemsHandler);
  }

  private MixinCompilationResult resolveReferencedBody(final IScope callerScope, final BodyOwner<?> mixin, final IScope mixinWorkingScope) {
    final ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(mixinWorkingScope, problemsHandler, configuration);

    final IScope referencedMixinScope = mixinWorkingScope;
    // ... and I'm starting to see the point of closures ...
    return InScopeSnapshotRunner.runInOriginalDataSnapshot(referencedMixinScope, new IFunction<MixinCompilationResult>() {

      @Override
      public MixinCompilationResult run() {
        // compile referenced mixin - keep the original copy unchanged
        List<ASTCssNode> replacement = compileBody(mixin.getBody(), referencedMixinScope);

        // collect variables and mixins to be imported
        IScope returnValues = ScopeFactory.createDummyScope();
        returnValues.addFilteredContent(new ImportedScopeFilter(expressionEvaluator, callerScope), referencedMixinScope);

        //FIXME: !!!!!!!!!! clean up
        return new MixinCompilationResult((ASTCssNode) mixin, replacement, returnValues);
      }

    });
  }

  private List<ASTCssNode> compileBody(Body body, IScope scopeSnapshot) {
    semiCompiledNodes.push(body);
    try {
      Body bodyClone = body.clone();
      parentSolver.unsafeDoSolveReferences(bodyClone, scopeSnapshot);
      return bodyClone.getMembers();
    } finally {
      semiCompiledNodes.pop();
    }
  }

  private void shiftComments(ASTCssNode reference, GeneralBody result) {
    List<ASTCssNode> childs = result.getMembers();
    if (!childs.isEmpty()) {
      childs.get(0).addOpeningComments(reference.getOpeningComments());
      childs.get(childs.size() - 1).addTrailingComments(reference.getTrailingComments());
    }
  }

  private IScope buildMixinsArguments(MixinReference reference, IScope referenceScope, FullMixinDefinition mixin) {
    ScopedValuesEvaluator referenceValuesEvaluator = new ScopedValuesEvaluator(referenceScope, problemsHandler, configuration);
    ScopedValuesEvaluator defaultValuesEvaluator = new ScopedValuesEvaluator(ScopeFactory.createJoinedScopesView(referenceScope, mixin.getScope()), problemsHandler, configuration);
    ArgumentsBuilder builder = new ArgumentsBuilder(reference, mixin.getMixin(), referenceValuesEvaluator, defaultValuesEvaluator, problemsHandler);
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
          IScope mixinWorkingScope = calculateBodyWorkingScope(callerScope, mixinArguments, mixinScope);

          MixinsGuardsValidator guardsValidator = new MixinsGuardsValidator(mixinWorkingScope, problemsHandler, configuration);
          GuardValue guardValue = guardsValidator.evaluateGuards(mixin);

          if (guardValue != GuardValue.DO_NOT_USE) {
            //OPTIMIZATION POSSIBLE: there is no need to compile mixins at this point, some of them are not going to be 
            //used and create snapshot operation is cheap now. It should be done later on.
            MixinCompilationResult compiled = resolveReferencedBody(callerScope, fullMixin.getMixin(), mixinWorkingScope);
            //mark the mixin according to its default() function use 
            compiled.setGuardValue(guardValue);
            //store the mixin as candidate
            compiledMixins.add(compiled);
          }
        }
      });
    }

    // filter out mixins we do not want to use  
    List<MixinCompilationResult> mixinsToBeUsed = defaultGuardHelper.chooseMixinsToBeUsed(compiledMixins, reference);

    // update mixin replacements and update scope with imported variables and mixins
    for (MixinCompilationResult compiled : mixinsToBeUsed) {
      result.addMembers(compiled.getReplacement());
      callerScope.addToDataPlaceholder(compiled.getReturnValues());
    }

    resolveImportance(reference, result);

    callerScope.closeDataPlaceholder();
    shiftComments(reference, result);

    return result;
  }

  public GeneralBody buildDetachedRulesetReplacement(DetachedRulesetReference reference, IScope callerScope, DetachedRuleset detachedRuleset, IScope detachedRulesetScope) {
    IScope mixinWorkingScope = calculateBodyWorkingScope(callerScope, null, detachedRulesetScope);
    MixinCompilationResult compiled = resolveReferencedBody(callerScope, detachedRuleset, mixinWorkingScope);
    GeneralBody result = new GeneralBody(reference.getUnderlyingStructure());

    result.addMembers(compiled.getReplacement());
    callerScope.addToDataPlaceholder(compiled.getReturnValues());
    callerScope.closeDataPlaceholder();

    //resolveImportance(reference, result);
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

  private static IScope calculateBodyWorkingScope(IScope callerScope, IScope arguments, IScope bodyScope) {
    // add arguments
    if (arguments != null) {
      IScope mixinDeclarationScope = bodyScope.getParent();
      mixinDeclarationScope.add(arguments);
    }

    // locally defined mixin does not require any other action
    boolean isLocallyDefined = bodyScope.seesLocalDataOf(callerScope);
    if (isLocallyDefined) {
      return bodyScope;
    }

    //join scopes
    IScope result = ScopeFactory.createJoinedScopesView(callerScope, bodyScope);
    return result;
  }

  class ImportedScopeFilter implements LocalScopeFilter {
    private final ExpressionEvaluator expressionEvaluator;
    private final IScope importTargetScope;

    public ImportedScopeFilter(ExpressionEvaluator expressionEvaluator, IScope importTargetScope) {
      super();
      this.expressionEvaluator = expressionEvaluator;
      this.importTargetScope = importTargetScope;
    }

    public Expression apply(Expression input) {
      return expressionEvaluator.evaluate(input);
    }

    @Override
    public FullNodeDefinition apply(FullNodeDefinition input) {
      if (input == null) {
        return null;
      }

      if (!(input.getNode() instanceof Expression)) {
        //FIXME !!!!!!!!!!!!!!!!! what to do here????
        return input;
      }
      Expression expression = (Expression)input.getNode();
      return new FullNodeDefinition(apply(expression), apply(input.getPrimaryScope())) ;
    }

    //FIXME: !!!!!!!!!!!!!!!!!! clean up
    @Override
    public IScope apply(IScope input) {
      if (input==null)
        return null;
      return constructImportedBodyScope(importTargetScope, input);
    }

    private ScopeView constructImportedBodyScope(IScope importTargetScope, IScope bodyToBeImportedScope) {
      ScopeView newScope = null;
      boolean isLocalImport = bodyToBeImportedScope.seesLocalDataOf(importTargetScope);
      if (isLocalImport) {
        // we need to copy the whole tree, because this runs inside referenced mixin scope 
        // snapshot and imported mixin needs to remember the scope as it is now 
        newScope = ScopeFactory.createJoinedScopesView(null, bodyToBeImportedScope);
        newScope.saveLocalDataForTheWholeWayUp();
      } else {
        // since this is non-local import, we need to join reference scope and imported mixins scope
        // imported mixin needs to have access to variables defined in caller
        newScope = ScopeFactory.createJoinedScopesView(importTargetScope, bodyToBeImportedScope);
        newScope.saveLocalDataForTheWholeWayUp();
      }
      return newScope;
    }

  }
}
