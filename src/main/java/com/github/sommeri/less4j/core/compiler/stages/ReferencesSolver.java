package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.Body;
import com.github.sommeri.less4j.core.ast.BodyOwner;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.Declaration;
import com.github.sommeri.less4j.core.ast.EscapedSelector;
import com.github.sommeri.less4j.core.ast.EscapedValue;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FixedNamePart;
import com.github.sommeri.less4j.core.ast.GeneralBody;
import com.github.sommeri.less4j.core.ast.IndirectVariable;
import com.github.sommeri.less4j.core.ast.InterpolableName;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.SimpleSelector;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.ast.VariableNamePart;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionEvaluator;
import com.github.sommeri.less4j.core.compiler.expressions.strings.StringInterpolator;
import com.github.sommeri.less4j.core.compiler.scopes.FullMixinDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.IteratedScope;
import com.github.sommeri.less4j.core.compiler.scopes.Scope;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.utils.QuotesKeepingInStringCssPrinter;
import com.github.sommeri.less4j.utils.debugonly.DebugSysout;

//FIXME: !!!! document does not pass through media 
public class ReferencesSolver {

  public static final String ALL_ARGUMENTS = "@arguments";
  private ASTManipulator manipulator = new ASTManipulator();
  private final ProblemsHandler problemsHandler;
  private StringInterpolator stringInterpolator = new StringInterpolator();

  public ReferencesSolver(ProblemsHandler problemsHandler) {
    this.problemsHandler = problemsHandler;
  }

  public void solveReferences(ASTCssNode node, Scope scope) {
    scope.createLocalDataSnapshot(); //FIXME: !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ERANBELASDASD
    doSolveReferences(node, new IteratedScope(scope));
    scope.discardLastLocalDataSnapshot(); //FIXME: !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ERANBELASDASD
  }

  private void doSolveReferences(ASTCssNode node, Scope scope) {
    doSolveReferences(node, new IteratedScope(scope));
  }

  private void doSolveReferences(ASTCssNode node, IteratedScope iteratedScope) {
    List<ASTCssNode> childs = new ArrayList<ASTCssNode>(node.getChilds());
    if (!childs.isEmpty()) {
      Scope scope = iteratedScope.getScope();

      // solve all mixin references and store solutions
      Map<MixinReference, GeneralBody> solvedMixinReferences = solveMixinReferences(childs, scope);

      // solve whatever is not a mixin reference
      solveNonMixinReferences(childs, iteratedScope);

      // replace mixin references by their solutions - we need to do it in the end
      // the scope and ast would get out of sync otherwise
      replaceMixinReferences(solvedMixinReferences);
    }
  }

  private void solveNonMixinReferences(List<ASTCssNode> childs, IteratedScope iteratedScope) {
    for (ASTCssNode kid : childs) {
      if (!isMixinReference(kid)) {
        if (AstLogic.hasOwnScope(kid)) {
          IteratedScope scope = iteratedScope.getNextChild();
          scope.getScope().createLocalDataSnapshot(); //FIXME: !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ERANBELASDASD
          doSolveReferences(kid, scope);
          scope.getScope().discardLastLocalDataSnapshot(); //FIXME: !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ERANBELASDASD
        } else {
          boolean finishedNode = solveIfVariableReference(kid, iteratedScope.getScope());
          if (!finishedNode)
            doSolveReferences(kid, iteratedScope);
        }
      }
    }
  }

  protected boolean isMixinReference(ASTCssNode kid) {
    return kid.getType() == ASTCssNodeType.MIXIN_REFERENCE;
  }

  private void replaceMixinReferences(Map<MixinReference, GeneralBody> solvedMixinReferences) {
    for (Entry<MixinReference, GeneralBody> entry : solvedMixinReferences.entrySet()) {
      MixinReference mixinReference = entry.getKey();
      GeneralBody replacement = entry.getValue();
      manipulator.replaceInBody(mixinReference, replacement.getMembers());
    }
  }

  private Map<MixinReference, GeneralBody> solveMixinReferences(List<ASTCssNode> childs, Scope scope) {
    Map<MixinReference, GeneralBody> solvedMixinReferences = new HashMap<MixinReference, GeneralBody>();
    for (ASTCssNode kid : childs) {
      if (isMixinReference(kid)) {
        MixinReference mixinReference = (MixinReference) kid;
        List<FullMixinDefinition> mixins = findReferencedMixins(mixinReference, scope);
        GeneralBody replacement = resolveReferencedMixins(mixinReference, scope, mixins);
        AstLogic.validateLessBodyCompatibility(mixinReference, replacement.getMembers(), problemsHandler);
        solvedMixinReferences.put(mixinReference, replacement);
      }
    }
    return solvedMixinReferences;
  }

  protected List<FullMixinDefinition> findReferencedMixins(MixinReference mixinReference, Scope scope) {
    List<FullMixinDefinition> sameNameMixins = scope.getNearestMixins(mixinReference, problemsHandler);
    if (sameNameMixins.isEmpty())
      problemsHandler.undefinedMixin(mixinReference);

    //FIXME: !!!!! create more reasonable error hanling, like do not continue if it is empty and other to not get no found with the same name and no match on the same place
    List<FullMixinDefinition> mixins = (new MixinsReferenceMatcher(scope, problemsHandler)).filter(mixinReference, sameNameMixins);
    if (mixins.isEmpty())
      problemsHandler.unmatchedMixin(mixinReference);

    return mixins;
  }

  private boolean solveIfVariableReference(ASTCssNode node, Scope scope) {
    ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(scope, problemsHandler);
    switch (node.getType()) {
    case VARIABLE: {
      Expression replacement = expressionEvaluator.evaluate((Variable) node);
      manipulator.replace(node, replacement);
      return true;
    }
    case INDIRECT_VARIABLE: {
      Expression replacement = expressionEvaluator.evaluate((IndirectVariable) node);
      manipulator.replace(node, replacement);
      return true;
    }
    case STRING_EXPRESSION: {
      Expression replacement = expressionEvaluator.evaluate((CssString) node);
      manipulator.replace(node, replacement);
      return true;
    }
    case ESCAPED_VALUE: {
      Expression replacement = expressionEvaluator.evaluate((EscapedValue) node);
      manipulator.replace(node, replacement);
      return true;
    }
    case ESCAPED_SELECTOR: {
      SimpleSelector replacement = interpolateEscapedSelector((EscapedSelector) node, expressionEvaluator);
      manipulator.replace(node, replacement);
      return true;
    }
    case FIXED_NAME_PART: {
      FixedNamePart part = (FixedNamePart) node;
      FixedNamePart replacement = interpolateFixedNamePart(part, expressionEvaluator);
      part.getParent().replaceMember(part, replacement);
      return true;
    }
    case VARIABLE_NAME_PART: {
      VariableNamePart part = (VariableNamePart) node;
      Expression value = expressionEvaluator.evaluate(part.getVariable());
      FixedNamePart fixedName = toFixedName(value, node.getUnderlyingStructure());
      part.getParent().replaceMember(part, interpolateFixedNamePart(fixedName, expressionEvaluator));
      return true;
    }
    }
    return false;
  }

  private FixedNamePart toFixedName(Expression value, HiddenTokenAwareTree parent) {
    QuotesKeepingInStringCssPrinter printer = new QuotesKeepingInStringCssPrinter();
    printer.append(value);
    // property based alternative would be nice, but does not seem to be needed
    FixedNamePart fixedName = new FixedNamePart(parent, printer.toString());
    return fixedName;
  }

  private SimpleSelector interpolateEscapedSelector(EscapedSelector input, ExpressionEvaluator expressionEvaluator) {
    HiddenTokenAwareTree underlying = input.getUnderlyingStructure();
    String value = stringInterpolator.replaceIn(input.getValue(), expressionEvaluator, input.getUnderlyingStructure());
    InterpolableName interpolableName = new InterpolableName(underlying, new FixedNamePart(underlying, value));
    return new SimpleSelector(input.getUnderlyingStructure(), interpolableName, false);
  }

  private FixedNamePart interpolateFixedNamePart(FixedNamePart input, ExpressionEvaluator expressionEvaluator) {
    String value = stringInterpolator.replaceIn(input.getName(), expressionEvaluator, input.getUnderlyingStructure());
    return new FixedNamePart(input.getUnderlyingStructure(), value);
  }

  // FIXME: !!!! the ugliest code ever seen, refactor it before commit - but
  // only after it starts to work !!!!
  private GeneralBody resolveReferencedMixins(MixinReference reference, Scope referenceScope, List<FullMixinDefinition> mixins) {
    GeneralBody result = new GeneralBody(reference.getUnderlyingStructure());
    for (FullMixinDefinition referencedMixin : mixins) {

      Scope mixinArguments = buildMixinsArguments(reference, referenceScope, referencedMixin);
      Scope referencedMixinWorkingScope = calculateMixinsWorkingScope(referenceScope, mixinArguments.copyWholeTree(), referencedMixin.getScope());

      ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(referencedMixinWorkingScope, problemsHandler);
      ReusableStructure mixin = referencedMixin.getMixin();

      if (expressionEvaluator.evaluate(mixin.getGuards())) {
        GeneralBody body = mixin.getBody().clone();
        referencedMixinWorkingScope.createLocalDataSnapshot(); //FIXME: !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ERANBELASDASD
        doSolveReferences(body, referencedMixinWorkingScope);
        result.addMembers(body.getMembers());

        List<FullMixinDefinition> allMixinsToImport = new ArrayList<FullMixinDefinition>();
        for (FullMixinDefinition mixinToImport : referencedMixinWorkingScope.getAllMixins()) {
          //debug.less a nested
          // FIXME: !!!!! tie scopes su v zlych poradiach - outer before inner
          // !! this is wrong:
          // [#default#] > [.threeBucket] > [.twoBucket] > [.oneBucket] > [#arguments-MixinReference[CssClass [.oneBucket]]#] > [#arguments-MixinReference[CssClass [.twoBucket]]#] > [.mixin]
          
          // FIXME: !!!! add unit test imports chain: local import, outside import
          // FIXME: !!!! zrusit toto: #standard#[#arguments-MixinReference[CssClass [.twoBucket]]#]C
          // FIXME: !!!! robit kopie len ak to naozaj treba
          Scope scopeTreeCopy = mixinToImport.getScope().copyWholeTree();
          if (referencedMixin.getScope().getParent()==referenceScope) {
            System.out.println("som tu"); // unreachable code
          }
          if (AstLogic.canHaveArguments(referencedMixin.getMixin())) { 
            Scope argumentsAwaitingParent = scopeTreeCopy.findFirstArgumentsAwaitingParent();
            // mixin can be imported either from inside or outside.
            
            if (argumentsAwaitingParent!=null) { // if it is from inside: = this is probably bad way to check it
              argumentsAwaitingParent.createDataClone();
              argumentsAwaitingParent.add(mixinArguments);
              argumentsAwaitingParent.setAwaitingArguments(false);
            } else { //if it is from outside:
              DebugSysout.println("I'm outside");
              scopeTreeCopy.getRootScope().setParent(referencedMixinWorkingScope.copyWithParentsChain());
            }
          }
          
          
          //tuto som skoncila
          
          
          
          //scopeTreeCopy.insertAsParent(mixinArguments.copyWholeTree());
          if (scopeTreeCopy.toString().contains("[#default#] > [.threeBucket] > [.twoBucket] > [.oneBucket] > [#arguments-MixinReference[CssClass [.oneBucket]]#] > [#arguments-MixinReference[CssClass [.twoBucket]]#] > [.mixin]")) {
            DebugSysout.println(1);
            System.out.println();
          }

          DebugSysout.println(mixinToImport.getMixin());
          DebugSysout.println(scopeTreeCopy);
          DebugSysout.println("---------------------------------------------");
          //
          allMixinsToImport.add(new FullMixinDefinition(mixinToImport.getMixin(), scopeTreeCopy));
        }

        // update scope
        Scope returnValues = expressionEvaluator.evaluateValues(referencedMixinWorkingScope);
        referencedMixinWorkingScope.discardLastLocalDataSnapshot(); //FIXME: !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ERANBELASDASD
        returnValues.addAllMixins(allMixinsToImport);
        referenceScope.addToPlaceholder(returnValues);
      }
    }

    referenceScope.closePlaceholder();
    resolveImportance(reference, result);
    shiftComments(reference, result);

    return result;
  }

  private void shiftComments(MixinReference reference, GeneralBody result) {
    List<ASTCssNode> childs = result.getMembers();
    if (!childs.isEmpty()) {
      childs.get(0).addOpeningComments(reference.getOpeningComments());
      childs.get(childs.size() - 1).addTrailingComments(reference.getTrailingComments());
    }
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

  public static Scope calculateMixinsWorkingScope(Scope callerScope, Scope arguments, Scope mixinsScope) {
    //FIXME: !!!! find out whether we really need to copy whole tree
    Scope result = mixinsScope.copyWholeTree();
    Scope callerScopeCopy = callerScope.copyWithParentsChain();

    result.insertAsParent(arguments);

    Scope resultRoot = result.getRootScope();
    resultRoot.setParent(callerScopeCopy);
    return result;
  }

  public static Scope calculateMixinsWorkingScopeAlt2(Scope callerScope, Scope arguments, Scope mixinsScope) {
    //FIXME: !!!! find out whether we really need to copy whole tree
    Scope result = mixinsScope.copyWholeTree();
    result.insertAsParent(arguments);

    Scope resultRoot = result.getRootScope();
    resultRoot.setParent(callerScope.copyWithParentsChain());
    return result;
  }

  private Scope buildMixinsArguments(MixinReference reference, Scope referenceScope, FullMixinDefinition mixin) {
    MixinArgumentsBuilder builder = new MixinArgumentsBuilder(reference, mixin.getMixin(), new ExpressionEvaluator(referenceScope, problemsHandler), problemsHandler);
    return builder.build();
  }

}
