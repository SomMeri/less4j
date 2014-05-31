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
import com.github.sommeri.less4j.core.compiler.scopes.FullNodeDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.ScopeFactory;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.utils.ArraysUtils;

class ArgumentsBuilder {

  // utils
  private final ProblemsHandler problemsHandler;
  private final ScopedValuesEvaluator referenceValuesEvaluator;
  private final ScopedValuesEvaluator defaultValuesEvaluator;
  private final String ALL_ARGUMENTS = ReferencesSolver.ALL_ARGUMENTS;

  // input
  private Iterator<Expression> positionalParameters;
  private ReusableStructure mixin;
  private MixinReference reference;

  // results
  private List<FullNodeDefinition> allValues = new ArrayList<FullNodeDefinition>();
  private IScope argumentsScope;

  public ArgumentsBuilder(MixinReference reference, ReusableStructure mixin, ScopedValuesEvaluator referenceValuesEvaluator, ScopedValuesEvaluator defaultValuesEvaluator, ProblemsHandler problemsHandler) {
    super();
    this.referenceValuesEvaluator = referenceValuesEvaluator;
    this.defaultValuesEvaluator = defaultValuesEvaluator;
    this.problemsHandler = problemsHandler;
    this.positionalParameters = reference.getPositionalParameters().iterator();
    this.reference = reference;

    argumentsScope = ScopeFactory.createDummyScope(reference, "#arguments-" + reference + "#");
    this.mixin = mixin;
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

    FullNodeDefinition allArgumentsValue = referenceValuesEvaluator.joinFull(allValues, reference);
    //FIXME!!!!!!!!!!!! scopeee if needed
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
    FullNodeDefinition value = referenceValuesEvaluator.toFullNodeDefinition(reference.getNamedParameter(declaration.getVariable()));
    allValues.add(value);
    argumentsScope.registerVariable(declaration, value);
  }

  private boolean canFillFromNamed(ArgumentDeclaration declaration) {
    return reference.hasNamedParameter(declaration.getVariable());
  }

  private void fillFromDefault(ArgumentDeclaration declaration) {
    FullNodeDefinition value = defaultValuesEvaluator.toFullNodeDefinition(declaration.getValue());
    allValues.add(value);
    argumentsScope.registerVariable(declaration, value);
  }

  private boolean hasDefault(ArgumentDeclaration declaration) {
    return declaration.getValue() != null;
  }

  private void fillFromPositional(ArgumentDeclaration declaration) {
    FullNodeDefinition value = referenceValuesEvaluator.toFullNodeDefinition(positionalParameters.next());
    allValues.add(value);
    argumentsScope.registerVariable(declaration, value);
  }

  private boolean canFillFromPositional() {
    return positionalParameters.hasNext();
  }

  private void addAsCollector(ArgumentDeclaration declaration) {
    List<Expression> allArgumentsFrom = referenceValuesEvaluator.evaluateAll(ArraysUtils.remaining(positionalParameters));
    Expression value = referenceValuesEvaluator.joinAll(allArgumentsFrom, reference);
    allValues.addAll(referenceValuesEvaluator.toFullNodeDefinitions(allArgumentsFrom));
    //FIXME!!!!!!!!!!!! scopeee if needed
    argumentsScope.registerVariable(declaration, new FullNodeDefinition(value, null));

  }

}