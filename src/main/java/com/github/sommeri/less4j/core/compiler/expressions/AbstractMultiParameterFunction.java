package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.ComposedExpression;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FaultyExpression;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

abstract class AbstractMultiParameterFunction extends AbstractFunction {

  @Override
  public Expression evaluate(Expression parameters, ProblemsHandler problemsHandler) {
    List<Expression> splitParameters;

    if (getMinParameters() == 1 && parameters.getType() != ASTCssNodeType.COMPOSED_EXPRESSION) {
      splitParameters = Collections.singletonList(parameters);
    } else if (parameters.getType() == ASTCssNodeType.COMPOSED_EXPRESSION) {
      splitParameters = ((ComposedExpression) parameters).splitByComma();
    } else {
      problemsHandler.wrongNumberOfArgumentsToFunction(parameters, getName(), getMinParameters());
      return new FaultyExpression(parameters);
    }

    if (splitParameters.size() >= getMinParameters() && splitParameters.size() <= getMaxParameters()) {
      /* Validate */
      boolean valid = true;
      for (int i = 0; i < splitParameters.size(); i++) {
        if (!validateParameter(splitParameters.get(i), i, problemsHandler)) {
          valid = false;
        }
      }

      if (valid) {
        return evaluate(splitParameters, problemsHandler, parameters.getUnderlyingStructure());
      } else {
        return new FaultyExpression(parameters);
      }
    } else {
      problemsHandler.wrongNumberOfArgumentsToFunction(parameters, getName(), getMinParameters());
      return new FaultyExpression(parameters);
    }
  }

  protected abstract Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, HiddenTokenAwareTree token);

  protected abstract int getMinParameters();

  protected abstract int getMaxParameters();

  protected abstract boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler);

  protected boolean validateParameter(Expression parameter, ProblemsHandler problemsHandler, ASTCssNodeType... expected) {
    Set<ASTCssNodeType> expectedSet = new HashSet<ASTCssNodeType>(Arrays.asList(expected));
    if (!expectedSet.contains(parameter.getType())) {
      problemsHandler.wrongArgumentTypeToFunction(parameter, getName(), parameter.getType(), expected);
      return false;
    } else {
      return true;
    }
  }
  
  protected abstract String getName();

}