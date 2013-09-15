package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.ArgumentDeclaration;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionEvaluator;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.ScopeFactory;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.utils.ArraysUtils;

class ArgumentsBuilder {

  // utils
  private final ProblemsHandler problemsHandler;
  private final ExpressionEvaluator referenceEvaluator;
  private final String ALL_ARGUMENTS = ReferencesSolver.ALL_ARGUMENTS;

  // input
  private Iterator<Expression> positionalParameters;
  private ReusableStructure mixin;
  private MixinReference reference;

  // results
  private List<Expression> allValues = new ArrayList<Expression>();
  private IScope argumentsScope;

  public ArgumentsBuilder(MixinReference reference, ReusableStructure pureMixin, ExpressionEvaluator referenceEvaluator, ProblemsHandler problemsHandler) {
    super();
    this.referenceEvaluator = referenceEvaluator;
    this.problemsHandler = problemsHandler;
    this.positionalParameters = reference.getPositionalParameters().iterator();
    this.reference = reference;

    argumentsScope = ScopeFactory.createDummyScope(reference, "#arguments-" + reference + "#");
    mixin = pureMixin;
  }

  public IScope build() {
    int length = mixin.getParameters().size();
    for (int i = 0; i < length; i++) {
      ASTCssNode parameter = mixin.getParameters().get(i);
      if (parameter.getType() == ASTCssNodeType.ARGUMENT_DECLARATION) {
        add((ArgumentDeclaration) parameter);
      } else {
        skipPositionalParameter();
      }

    }

    Expression allArgumentsValue = referenceEvaluator.joinAll(allValues, reference);
    argumentsScope.registerVariableIfNotPresent(ALL_ARGUMENTS, allArgumentsValue);
    return argumentsScope;
  }

  private void skipPositionalParameter() {
    positionalParameters.next();
  }

  private void add(ArgumentDeclaration declaration) {
    if (canFillFromNamed(declaration)) {
      fillFromNamed(declaration);
    } else if (declaration.isCollector()) {
      addAsCollector(declaration);
    } else if (canFillFromPositional()) {
      fillFromPositional(declaration);
    } else if (hasDefault(declaration)) {
      fillFromDefault(declaration);
    } else {
      if (declaration.getValue() == null)
        problemsHandler.undefinedMixinParameterValue(mixin, declaration, reference);
    }

  }

  private void fillFromNamed(ArgumentDeclaration declaration) {
    Expression value = referenceEvaluator.evaluate(reference.getNamedParameter(declaration.getVariable()));
    allValues.add(value);
    argumentsScope.registerVariable(declaration, value);
  }

  private boolean canFillFromNamed(ArgumentDeclaration declaration) {
    return reference.hasNamedParameter(declaration.getVariable());
  }

  private void fillFromDefault(ArgumentDeclaration declaration) {
    allValues.add(declaration.getValue());
    argumentsScope.registerVariable(declaration);
  }

  private boolean hasDefault(ArgumentDeclaration declaration) {
    return declaration.getValue() != null;
  }

  private void fillFromPositional(ArgumentDeclaration declaration) {
    Expression value = referenceEvaluator.evaluate(positionalParameters.next());
    allValues.add(value);
    argumentsScope.registerVariable(declaration, value);
  }

  private boolean canFillFromPositional() {
    return positionalParameters.hasNext();
  }

  private void addAsCollector(ArgumentDeclaration declaration) {
    List<Expression> allArgumentsFrom = referenceEvaluator.evaluateAll(ArraysUtils.remaining(positionalParameters));
    allValues.addAll(allArgumentsFrom);
    Expression value = referenceEvaluator.joinAll(allArgumentsFrom, reference);
    argumentsScope.registerVariable(declaration, value);

  }

}