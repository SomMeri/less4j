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
import com.github.sommeri.less4j.core.compiler.scopes.InScopeSnapshotRunner.IFunction;
import com.github.sommeri.less4j.core.compiler.scopes.InScopeSnapshotRunner.ITask;
import com.github.sommeri.less4j.core.compiler.scopes.RequestCollector;
import com.github.sommeri.less4j.core.compiler.scopes.Scope;
import com.github.sommeri.less4j.core.compiler.scopes.ScopeView;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.utils.ArraysUtils;

class MixinsSolver {

  private final ProblemsHandler problemsHandler;
  private final ReferencesSolver parentSolver;
  private final AstNodesStack semiCompiledNodes;

  private final CompiledMixinsCache cache = new CompiledMixinsCache();

  public MixinsSolver(ReferencesSolver parentSolver, AstNodesStack semiCompiledNodes, ProblemsHandler problemsHandler) {
    this.parentSolver = parentSolver;
    this.semiCompiledNodes = semiCompiledNodes;
    this.problemsHandler = problemsHandler;
  }

  private MixinCompilationResult resolveMixinReference(final Scope callerScope, final FullMixinDefinition referencedMixin, final Scope mixinWorkingScope, final ExpressionEvaluator expressionEvaluator) {
    final ReusableStructure mixin = referencedMixin.getMixin();
    final Scope mixinScope = referencedMixin.getScope();

    MixinCompilationResult compiled = null;
    compiled = cache.getCompiled(mixin, mixinScope, mixinWorkingScope);
    if (compiled != null) {
      compiled.setCached(true);

      List<FullMixinDefinition> allMixinsToImport = mixinsToImport(callerScope, mixinWorkingScope, compiled.getUnmodifiedMixinsToImport());
      Scope originalReturnValues = compiled.getReturnValues();//.replaceAllMixins(allMixinsToImport);
      Scope returnValues = Scope.createDummyScope();
      returnValues.addVariables(originalReturnValues);
      returnValues.addAllMixins(allMixinsToImport);
      compiled.setReturnValues(returnValues);
      return compiled;
    }
    //    System.out.println("Compiling: " + mixin.getNamesAsStrings() + " scopes created: " + Scope.copiedScope);

    final Scope referencedMixinScope = mixinWorkingScope;
    // ... and I'm starting to see the point of closures ...
    return InScopeSnapshotRunner.runInLocalDataSnapshot(referencedMixinScope, new IFunction<MixinCompilationResult>() {

      @Override
      public MixinCompilationResult run() {
        // compile referenced mixin - keep the original copy unchanged
        List<ASTCssNode> replacement = compileReferencedMixin(mixin, referencedMixinScope);

        // collect variables and mixins to be imported
        Scope returnValues = expressionEvaluator.evaluateValues(referencedMixinScope);
        List<FullMixinDefinition> unmodifiedMixinsToImport = referencedMixinScope.getAllMixins();

        List<FullMixinDefinition> allMixinsToImport = mixinsToImport(callerScope, referencedMixinScope, unmodifiedMixinsToImport);
        returnValues.addAllMixins(allMixinsToImport);

        return new MixinCompilationResult(replacement, returnValues, unmodifiedMixinsToImport);
      }

    });
  }

  //TODO: all these methods names are too similar to each other - better and clearer naming is needed
  private List<ASTCssNode> compileReferencedMixin(ReusableStructure referencedMixin, Scope referencedMixinScopeSnapshot) {
    semiCompiledNodes.push(referencedMixin);
    try {
      GeneralBody bodyClone = referencedMixin.getBody().clone();
      parentSolver.unsafeDoSolveReferences(bodyClone, referencedMixinScopeSnapshot);
      return bodyClone.getMembers();
    } finally {
      semiCompiledNodes.pop();
    }
  }

  private List<FullMixinDefinition> mixinsToImport(Scope referenceScope, Scope referencedMixinScope, List<FullMixinDefinition> unmodifiedMixinsToImport) {
    List<FullMixinDefinition> result = new ArrayList<FullMixinDefinition>();
    for (FullMixinDefinition mixinToImport : unmodifiedMixinsToImport) {
      boolean isLocalImport = mixinToImport.getScope().seesLocalDataOf(referenceScope);
      if (isLocalImport) {
        // we need to copy the whole tree, because this runs inside referenced mixin scope 
        // snapshot and imported mixin needs to remember the scope as it is now 
//        Scope oldWay = mixinToImport.getScope().copyWholeTree();
        ScopeView newWay = new ScopeView(mixinToImport.getScope(), null);
        newWay.saveLocalDataForTheWholeWayUp();
        result.add(new FullMixinDefinition(mixinToImport.getMixin(), newWay));
      } else {
        // since this is non-local import, we need to join reference scope and imported mixins scope
        // imported mixin needs to have access to variables defined in caller
        //FIXME: (!!!) less important HHHHHHHHHHHHHHEEEEEEEEEEEEEEEEEEEEHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHh
        //        Scope oldWay = mixinToImport.getScope().copyWholeTree();
        //        oldWay.getRootScope().setParent(referencedMixinScope.copyWithParentsChain());

        ScopeView newWay = new ScopeView(mixinToImport.getScope(), referencedMixinScope);
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
          System.out.println(reference);
          System.out.println("mixinScope: " +mixinScope.getValue("@width"));
          System.out.println("mixinArguments: " +mixinArguments.getValue("@width"));
          System.out.println("callerScope: " +callerScope.getValue("@width"));
          Scope mixinWorkingScope = calculateMixinsWorkingScope(callerScope, mixinArguments, mixinScope);
          System.out.println("mixinWorkingScope: " + mixinWorkingScope.getValue("@width"));

          RequestCollector requestCollector = new RequestCollector();
          if (mixinWorkingScope.hasParent())
            mixinWorkingScope.getParent().addRequestCollector(requestCollector);

          ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(mixinWorkingScope, problemsHandler);
          if (expressionEvaluator.guardsSatisfied(mixin)) {
            MixinCompilationResult compiled = resolveMixinReference(callerScope, fullMixin, mixinWorkingScope, expressionEvaluator);

            // update mixin replacements and update scope with imported variables and mixins
            result.addMembers(compiled.getReplacement());
            callerScope.addToPlaceholder(compiled.getReturnValues());
            //            Scope interestingScope = callerScope.getAllMixins().get(0).getScope();
            //            Scope scopeThatGotIt = interestingScope.getParent().getParent().getParent().getParent().getParent().getParent();
            //            System.out.println(scopeThatGotIt.getValue("@width"));
            //            System.out.println(scopeThatGotIt.getParent().getValue("@width"));
            //            System.out.println(scopeThatGotIt.getValue("@width"));
            //            System.out.println("asdf");

            //requestCollector.printAllCollected();
            if (!requestCollector.usedScope() && !compiled.wasCached()) { //FIXME: (!!!!) I have no idea why was cached is important there ... Do not commit until you understand what you created, right?
              cache.theMixinUsedOnlyHisOwnScope(mixin, mixinScope, compiled, requestCollector);
            }
          }
          if (mixinWorkingScope.hasParent())
            mixinWorkingScope.getParent().removeRequestCollector(requestCollector);

        }
      });

    }

    //    Scope whywasthisdiscardedtoo = callerScope.getAllMixins().get(0).getScope();
    //    System.out.println("whywasthisdiscardedtoo: " + whywasthisdiscardedtoo.getValue("@width"));
    //    Scope lastWithout = whywasthisdiscardedtoo.getParent().getParent();
    //    System.out.println(lastWithout.getValue("@width"));
    //    System.out.println(lastWithout.getParent().getValue("@width"));
    //    System.out.println(lastWithout.getValue("@width"));
    //    Scope scopeThatShouldHaveIt = whywasthisdiscardedtoo.getParent().getParent().getParent().getParent().getParent().getParent();
    //    System.out.println("scopeThatShouldHaveIt: " + scopeThatShouldHaveIt.getValue("@width"));
    if (callerScope.getAllMixins().size() >= 3) {
      System.out.println(callerScope.getAllMixins().get(2).getMixin());
      System.out.println(callerScope.getAllMixins().get(2).getScope().getValue("@width"));
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
    boolean isLocallyDefined = mixinDeclarationScope.seesLocalDataOf(callerScope);
    if (isLocallyDefined) {
      return mixinScope;
    }

    //join scopes
    //FIXME: (!!!) HHHHHHHHHHHHHHEEEEEEEEEEEEEEEEEEEEHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHh
    //    Scope result = mixinScope.copyWholeTree();
    //    Scope resultRootScope = result.getRootScope();
    //    resultRootScope.setParent(callerScope.copyWithParentsChain());

    //result = new ScopeJointChild(callerScope, resultRootScope);
    Scope result = new ScopeView(mixinScope, callerScope);
//    Scope lastCorrect = mixinScope.getParent().getParent().getParent();
//    System.out.println(lastCorrect.getLocalValue("@width"));
//    System.out.println(lastCorrect.getValue("@width"));
//    Scope firstWrong = lastCorrect.getParent();
//    System.out.println(result.getParent().getParent().getParent().getValue("@width"));
//    System.out.println(result.getValue("@width"));
    return result;
  }

}

class MixinCompilationResult {

  private List<ASTCssNode> replacement;
  private Scope returnValues;
  private boolean cached = false;
  private List<FullMixinDefinition> unmodifiedMixinsToImport;

  public MixinCompilationResult(List<ASTCssNode> replacement, Scope returnValues, List<FullMixinDefinition> unmodifiedMixinsToImport) {
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

  public Scope getReturnValues() {
    return returnValues;
  }

  public void setReturnValues(Scope returnValues) {
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