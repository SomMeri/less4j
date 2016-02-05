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
import com.github.sommeri.less4j.core.ast.KeywordExpression;
import com.github.sommeri.less4j.core.ast.ListExpression;
import com.github.sommeri.less4j.core.ast.ListExpressionOperator;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionEvaluator;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionFilter;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionManipulator;
import com.github.sommeri.less4j.core.compiler.expressions.GuardValue;
import com.github.sommeri.less4j.core.compiler.expressions.MixinsGuardsValidator;
import com.github.sommeri.less4j.core.compiler.scopes.FoundMixin;
import com.github.sommeri.less4j.core.compiler.scopes.FullMixinDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.InScopeSnapshotRunner;
import com.github.sommeri.less4j.core.compiler.scopes.InScopeSnapshotRunner.IFunction;
import com.github.sommeri.less4j.core.compiler.scopes.InScopeSnapshotRunner.ITask;
import com.github.sommeri.less4j.core.compiler.scopes.ScopeFactory;
import com.github.sommeri.less4j.core.compiler.scopes.view.ScopeView;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.core.problems.UnableToFinish;
import com.github.sommeri.less4j.utils.ArraysUtils;
import com.github.sommeri.less4j.utils.Couple;

class MixinsRulesetsSolver {

  private final ProblemsHandler problemsHandler;
  private final ReferencesSolver parentSolver;
  private final AstNodesStack semiCompiledNodes;
  private final Configuration configuration;
  private final DefaultGuardHelper defaultGuardHelper;
  private final CallerCalleeScopeJoiner scopeManipulation = new CallerCalleeScopeJoiner();
  private final ExpressionManipulator expressionManipulator = new ExpressionManipulator();

  public MixinsRulesetsSolver(ReferencesSolver parentSolver, AstNodesStack semiCompiledNodes, ProblemsHandler problemsHandler, Configuration configuration) {
    this.parentSolver = parentSolver;
    this.semiCompiledNodes = semiCompiledNodes;
    this.problemsHandler = problemsHandler;
    this.configuration = configuration;
    this.defaultGuardHelper = new DefaultGuardHelper(problemsHandler);
  }

  private Couple<List<ASTCssNode>, IScope> resolveCalledBody(final IScope callerScope, final BodyOwner<?> bodyOwner, final IScope referencedMixinScope, final ReturnMode returnMode, final int visibilityBlocks) {

    // ... and I'm starting to see the point of closures ...
    return InScopeSnapshotRunner.runInLocalDataSnapshot(referencedMixinScope, new IFunction<Couple<List<ASTCssNode>, IScope>>() {

      @Override
      public Couple<List<ASTCssNode>, IScope> run() {
        // compile referenced mixin - keep the original copy unchanged
        List<ASTCssNode> replacement = compileBody(bodyOwner.getBody(), referencedMixinScope);
        for (ASTCssNode node : replacement) {
          node.addVisibilityBlocks(visibilityBlocks);
        }

        // collect variables and mixins to be imported
        IScope returnValues = ScopeFactory.createDummyScope();
        if (returnMode == ReturnMode.MIXINS_AND_VARIABLES) {
          ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(referencedMixinScope, problemsHandler, configuration);
          returnValues.addFilteredVariables(new ImportedScopeFilter(expressionEvaluator, callerScope), referencedMixinScope);
        }
        List<FullMixinDefinition> unmodifiedMixinsToImport = referencedMixinScope.getAllMixins();

        List<FullMixinDefinition> allMixinsToImport = scopeManipulation.mixinsToImport(callerScope, referencedMixinScope, unmodifiedMixinsToImport);
        returnValues.addAllMixins(allMixinsToImport);

        return new Couple<List<ASTCssNode>, IScope>(replacement, returnValues);
      }

    });
  }

  private List<ASTCssNode> compileBody(Body body, IScope scopeSnapshot) {
    semiCompiledNodes.push(body.getParent());
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

  private IScope buildMixinsArguments(EvaluatedMixinReferenceCall evaluatedReference, IScope referenceScope, FullMixinDefinition mixin) {
    ArgumentsBuilder builder = new ArgumentsBuilder(evaluatedReference, mixin.getMixin(), problemsHandler);
    return builder.build();
  }

  public GeneralBody buildMixinReferenceReplacement(final EvaluatedMixinReferenceCall evaluatedReference, final IScope callerScope, List<FoundMixin> mixins) {
    final MixinReference reference = evaluatedReference.getReference();
    if (Thread.currentThread().isInterrupted())
      throw new UnableToFinish("Thread Interrupted", (ASTCssNode) null);

    final GeneralBody result = new GeneralBody(reference.getUnderlyingStructure());
    if (mixins.isEmpty())
      return result;

    // candidate mixins with information about their default() function use are
    // stored here
    final List<BodyCompilationData> readyMixins = new ArrayList<BodyCompilationData>();

    for (final FoundMixin fullMixin : mixins) {
      final ReusableStructure mixin = fullMixin.getMixin();
      final IScope mixinScope = fullMixin.getScope();

      final BodyCompilationData data = new BodyCompilationData(mixin);
      // the following needs to run in snapshot because
      // calculateMixinsWorkingScope modifies that scope
      InScopeSnapshotRunner.runInLocalDataSnapshot(mixinScope.getParent(), new ITask() {

        @Override
        public void run() {
          // add arguments
          IScope mixinArguments = buildMixinsArguments(evaluatedReference, callerScope, fullMixin);
          data.setArguments(mixinArguments);
          mixinScope.getParent().add(mixinArguments);
          ScopeView mixinWorkingScope = scopeManipulation.joinIfIndependent(callerScope, mixinScope);
          // it the mixin calls itself recursively, each copy should work on
          // independent copy of local data
          // that is matters mostly for scope placeholders - if both close
          // placeholders in same copy error happen
          mixinWorkingScope.toIndependentWorkingCopy();
          data.setMixinWorkingScope(mixinWorkingScope);

          MixinsGuardsValidator guardsValidator = new MixinsGuardsValidator(mixinWorkingScope, problemsHandler, configuration);
          GuardValue guardValue = guardsValidator.evaluateGuards(fullMixin.getGuardsOnPath(), mixin);
          data.setGuardValue(guardValue);

          readyMixins.add(data);
        }
      }); // end of InScopeSnapshotRunner.runInLocalDataSnapshot
    }

    // filter out mixins we do not want to use
    List<BodyCompilationData> mixinsToBeUsed = defaultGuardHelper.chooseMixinsToBeUsed(readyMixins, reference);

    for (final BodyCompilationData data : mixinsToBeUsed) {
      final ScopeView mixinWorkingScope = data.getMixinWorkingScope();

      // compilation must run in another localDataSnapshot, because imported
      // detached ruleset stored in
      // variables point to original scope - making snapshot above is not enough
      // since they point to scope as defined during definition, they would not
      // know parameters
      // of mixins that define them
      InScopeSnapshotRunner.runInLocalDataSnapshot(mixinWorkingScope.getParent(), new ITask() {

        @Override
        public void run() {
          BodyOwner<?> mixin = data.getCompiledBodyOwner();
          // add arguments again - detached rulesets imported into this one
          // via returned variables from sub-calls need would not see arguments
          // otherwise
          // bc they keep link to original copy and there is no other way how to
          // access them
          IScope arguments = data.getArguments();
          mixinWorkingScope.getParent().add(arguments);

          Couple<List<ASTCssNode>, IScope> compiled = resolveCalledBody(callerScope, mixin, mixinWorkingScope, ReturnMode.MIXINS_AND_VARIABLES, reference.getVisibilityBlocks());
          // update mixin replacements and update scope with imported variables
          // and mixins
          result.addMembers(compiled.getT());
          callerScope.addToDataPlaceholder(compiled.getM());
        }
      }); // end of InScopeSnapshotRunner.runInLocalData........Snapshot

    }

    callerScope.closeDataPlaceholder();
    resolveImportance(reference, result);
    shiftComments(reference, result);

    return result;
  }

  public GeneralBody buildDetachedRulesetReplacement(DetachedRulesetReference reference, IScope callerScope, DetachedRuleset detachedRuleset, IScope detachedRulesetScope) {
    IScope mixinWorkingScope = scopeManipulation.joinIfIndependent(callerScope, detachedRulesetScope);
    Couple<List<ASTCssNode>, IScope> compiled = resolveCalledBody(callerScope, detachedRuleset, mixinWorkingScope, ReturnMode.MIXINS, reference.getVisibilityBlocks());
    GeneralBody result = new GeneralBody(reference.getUnderlyingStructure());

    result.addMembers(compiled.getT());
    callerScope.addToDataPlaceholder(compiled.getM());
    callerScope.closeDataPlaceholder();

    // resolveImportance(reference, result);
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
        addImportantKeyword(declaration);
      } else if (kid instanceof BodyOwner<?>) {
        BodyOwner owner = (BodyOwner) kid;
        declarationsAreImportant(owner.getBody());
      }
    }
  }

  private void addImportantKeyword(Declaration declaration) {
    Expression expression = declaration.getExpression();
    if (expressionManipulator.isImportant(expression))
      return;

    HiddenTokenAwareTree underlying = expression != null ? expression.getUnderlyingStructure() : declaration.getUnderlyingStructure();
    KeywordExpression important = createImportantKeyword(underlying);

    ListExpression list = expressionManipulator.findRightmostSpaceSeparatedList(expression);
    if (list == null) {
      list = new ListExpression(underlying, ArraysUtils.asNonNullList(expression), new ListExpressionOperator(underlying, ListExpressionOperator.Operator.EMPTY_OPERATOR));
    }
    list.addExpression(important);
    list.configureParentToAllChilds();

    declaration.setExpression(list);
    list.setParent(declaration);
  }

  private KeywordExpression createImportantKeyword(HiddenTokenAwareTree underlyingStructure) {
    return new KeywordExpression(underlyingStructure, "!important", true);
  }

  class ImportedScopeFilter implements ExpressionFilter {

    private final ExpressionEvaluator expressionEvaluator;
    private final IScope importTargetScope;
    private final CallerCalleeScopeJoiner scopeManipulation = new CallerCalleeScopeJoiner();

    public ImportedScopeFilter(ExpressionEvaluator expressionEvaluator, IScope importTargetScope) {
      super();
      this.expressionEvaluator = expressionEvaluator;
      this.importTargetScope = importTargetScope;
    }

    public Expression apply(Expression input) {
      Expression result = expressionEvaluator.evaluate(input);
      IScope newScope = apply(result.getScope());
      result.setScope(newScope);
      return result;
    }

    private IScope apply(IScope input) {
      if (input == null)
        return importTargetScope;

      return scopeManipulation.joinIfIndependentAndPreserveContent(importTargetScope, input);
    }

    @Override
    public boolean accepts(String name, Expression value) {
      return true;
    }

  }

}