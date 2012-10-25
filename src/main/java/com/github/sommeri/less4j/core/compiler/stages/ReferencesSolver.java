package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.ArgumentDeclaration;
import com.github.sommeri.less4j.core.ast.Declaration;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.IndirectVariable;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.NamespaceReference;
import com.github.sommeri.less4j.core.ast.PureMixin;
import com.github.sommeri.less4j.core.ast.RuleSetsBody;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.compiler.CompileException;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionEvaluator;
import com.github.sommeri.less4j.core.compiler.scopes.FullMixinDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.IteratedScope;
import com.github.sommeri.less4j.core.compiler.scopes.ReferencedMixinScope;
import com.github.sommeri.less4j.core.compiler.scopes.Scope;

public class ReferencesSolver {

  private static final String ALL_ARGUMENTS = "@arguments";
  private ASTManipulator manipulator = new ASTManipulator();

  public void solveReferences(ASTCssNode node, Scope scope) {
    doSolveReferences(node, new IteratedScope(scope));
  }

  private void doSolveReferences(ASTCssNode node, Scope scope) {
    doSolveReferences(node, new IteratedScope(scope));
  }

  private void doSolveReferences(ASTCssNode node, IteratedScope scope) {
    ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(scope.getScope());

    switch (node.getType()) {
    case VARIABLE: {
      Expression replacement = expressionEvaluator.evaluate((Variable) node);
      manipulator.replace(node, replacement);
      break;
    }
    case INDIRECT_VARIABLE: {
      Expression replacement = expressionEvaluator.evaluate((IndirectVariable) node);
      manipulator.replace(node, replacement);
      break;
    }
    case MIXIN_REFERENCE: {
      MixinReference mixinReference = (MixinReference) node;
      RuleSetsBody replacement = resolveMixinReference(mixinReference, scope.getScope());
      manipulator.replaceInBody(mixinReference, replacement.getChilds());
      break;
    }
    case NAMESPACE_REFERENCE: {
      NamespaceReference namespaceReference = (NamespaceReference) node;
      RuleSetsBody replacement = resolveNamespaceReference(namespaceReference, scope.getScope());
      manipulator.replaceInBody(namespaceReference, replacement.getChilds());
      break;
    }
    }

    if (node.getType()!=ASTCssNodeType.NAMESPACE_REFERENCE) {
      List<ASTCssNode> childs = new ArrayList<ASTCssNode>(node.getChilds());
      for (ASTCssNode kid : childs) {
        if (AstLogic.hasOwnScope(kid)) {
          doSolveReferences(kid, new IteratedScope(scope.getNextChild()));
        } else {
          doSolveReferences(kid, scope);
        }
      }
    }
  }

  private RuleSetsBody resolveMixinReference(MixinReference reference, Scope scope) {
    List<FullMixinDefinition> sameNameMixins = scope.getNearestMixins(reference);
    return resolveReferencedMixins(reference, scope, sameNameMixins);
  }

  private RuleSetsBody resolveReferencedMixins(MixinReference reference, Scope referenceScope, List<FullMixinDefinition> sameNameMixins) {
    if (sameNameMixins.isEmpty())
      throw CompileException.createUndeclaredMixin(reference);

    List<FullMixinDefinition> mixins = (new MixinsReferenceMatcher(referenceScope)).filter(reference, sameNameMixins);
    if (mixins.isEmpty())
      CompileException.warnNoMixinsMatch(reference);

    RuleSetsBody result = new RuleSetsBody(reference.getUnderlyingStructure());
    for (FullMixinDefinition fullMixin : mixins) {
      Scope combinedScope = calculateMixinsOwnVariables(reference, referenceScope, fullMixin);
      ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(combinedScope);

      PureMixin mixin = fullMixin.getMixin();
      if (expressionEvaluator.evaluate(mixin.getGuards())) {
        RuleSetsBody body = mixin.getBody().clone();
        doSolveReferences(body, combinedScope);
        result.addMembers(body.getChilds());
      }
    }

    resolveImportance(reference, result);
    shiftComments(reference, result);

    return result;
  }

  private void shiftComments(MixinReference reference, RuleSetsBody result) {
    List<ASTCssNode> childs = result.getChilds();
    if (!childs.isEmpty()) {
      childs.get(0).addOpeningComments(reference.getOpeningComments());
      childs.get(childs.size() - 1).addTrailingComments(reference.getTrailingComments());
    }
  }

  private void resolveImportance(MixinReference reference, RuleSetsBody result) {
    if (reference.isImportant()) {
      declarationsAreImportant(result);
    }
  }

  private void declarationsAreImportant(RuleSetsBody result) {
    for (ASTCssNode kid : result.getChilds()) {
      if (kid instanceof Declaration) {
        Declaration declaration = (Declaration) kid;
        declaration.setImportant(true);
      }
    }
  }

  private Scope calculateMixinsOwnVariables(MixinReference reference, Scope referenceScope, FullMixinDefinition mixin) {
    //FIXME: createDefaultScope is a BUUUUUUUUUUUUUUUUUUUUUG
    Scope joinScopes = ReferencedMixinScope.joinScopes(mixin.getScope(), buildMixinsArgumentsScope(reference, referenceScope, mixin), referenceScope);
    return joinScopes;
  }

  private Scope buildMixinsArgumentsScope(MixinReference reference, Scope referenceScope, FullMixinDefinition mixin) {
    Scope variablesScope = Scope.createScope(reference, "#arguments-" + reference + "#", null);
    ExpressionEvaluator referenceEvaluator = new ExpressionEvaluator(referenceScope);

    List<Expression> allValues = new ArrayList<Expression>();

    int length = mixin.getMixin().getParameters().size();
    for (int i = 0; i < length; i++) {
      ASTCssNode parameter = mixin.getMixin().getParameters().get(i);
      if (parameter.getType() == ASTCssNodeType.ARGUMENT_DECLARATION) {
        ArgumentDeclaration declaration = (ArgumentDeclaration) parameter;
        if (declaration.isCollector()) {
          List<Expression> allArgumentsFrom = referenceEvaluator.evaluateAll(reference.getAllArgumentsFrom(i));
          allValues.addAll(allArgumentsFrom);
          Expression value = referenceEvaluator.joinAll(allArgumentsFrom, reference);
          variablesScope.registerVariable(declaration, value);
        } else if (reference.hasParameter(i)) {
          Expression value = referenceEvaluator.evaluate(reference.getParameter(i));
          allValues.add(value);
          variablesScope.registerVariable(declaration, value);
        } else {
          if (declaration.getValue() == null)
            CompileException.throwUndefinedMixinParameterValue(mixin.getMixin(), declaration, reference);

          allValues.add(declaration.getValue());
          variablesScope.registerVariable(declaration);
        }
      }
    }

    Expression compoundedValues = referenceEvaluator.joinAll(allValues, reference);
    variablesScope.registerVariableIfNotPresent(ALL_ARGUMENTS, compoundedValues);
    return variablesScope;
  }

  private RuleSetsBody resolveNamespaceReference(NamespaceReference reference, Scope scope) {
    List<FullMixinDefinition> sameNameMixins = scope.getNearestMixins(reference);
    return resolveReferencedMixins(reference.getFinalReference(), scope, sameNameMixins);
  }
}
