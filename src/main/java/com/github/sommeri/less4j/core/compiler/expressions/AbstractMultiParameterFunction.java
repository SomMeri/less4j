package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.ComposedExpression;
import com.github.sommeri.less4j.core.ast.Expression;
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
      problemsHandler.wrongNumberOfArgumentsToFunction(parameters, getMinParameters());
      return null;
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
        return null;
      }
    } else {
      problemsHandler.wrongNumberOfArgumentsToFunction(parameters, getMinParameters());
      return null;
    }
  }

  protected abstract Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, HiddenTokenAwareTree token);

  protected abstract int getMinParameters();

  protected abstract int getMaxParameters();

  protected abstract boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler);

  protected boolean validateParameter(Expression parameter, ASTCssNodeType expected, ProblemsHandler problemsHandler) {
    if (parameter.getType() != expected) {
      problemsHandler.wrongArgumentTypeToFunction(parameter, expected, parameter.getType());
      return false;
    } else {
      return true;
    }
  }

}