package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.List;

import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

/**
 * The function will accept only parameters compatible with what
 * functions do. No error or warning is thrown if they are not compatible.
 *
 * Extend this if there is a name clash between the function name and css3 function or filter.
 */
public abstract class CssNameClashMultiParameterFunction extends AbstractMultiParameterFunction {

  @Override
  public boolean acceptsParameters(List<Expression> parameters) {
    if (parameters.size() < getMinParameters() || parameters.size() > getMaxParameters()) {
      return false;
    }

    for (int i = 0; i < parameters.size(); i++) {
      // this problems handler will swallow and ignore all errors and warnings
      if (!validateParameter(parameters.get(i), i, new ProblemsHandler())) {
        return false;
      }
    }

    return true;
  }
  
}
