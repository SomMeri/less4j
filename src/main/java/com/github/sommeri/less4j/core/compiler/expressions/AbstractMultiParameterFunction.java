package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public abstract class AbstractMultiParameterFunction extends AbstractFunction {

  protected abstract int getMinParameters();

  protected abstract int getMaxParameters();

  protected abstract String getName();

  protected abstract boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler);

  protected boolean validateParameterTypeReportError(Expression parameter, ProblemsHandler problemsHandler, ASTCssNodeType... expected) {
    boolean isValid = validateParameterTypeDoNotReport(parameter, problemsHandler, expected);
    if (!isValid) {
      problemsHandler.wrongArgumentTypeToFunction(parameter, getName(), parameter.getType(), expected);
    }
    return isValid;
  }

  protected boolean validateParameterTypeDoNotReport(Expression parameter, ProblemsHandler problemsHandler, ASTCssNodeType... expected) {
    Set<ASTCssNodeType> expectedSet = new HashSet<ASTCssNodeType>(Arrays.asList(expected));
    if (!expectedSet.contains(parameter.getType())) {
      return false;
    } else {
      return true;
    }
  }

}
