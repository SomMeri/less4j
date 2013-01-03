package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.HashMap;
import java.util.Map;

import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FaultyExpression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression.Dimension;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class UnknownFunctions implements FunctionsPackage {

  private final ProblemsHandler problemsHandler;

  public UnknownFunctions(ProblemsHandler problemsHandler) {
    this.problemsHandler = problemsHandler;
  }

  @Override
  public boolean canEvaluate(FunctionExpression input, Expression parameters) {
    return true;
  }
  
  @Override
  public Expression evaluate(FunctionExpression input, Expression parameters) {
    if (!canEvaluate(input, parameters))
      return input;

    Expression oldParameter = input.getParameter();
    oldParameter.setParent(null);
    input.setParameter(parameters);
    input.configureParentToAllChilds();
    
    return input;
  }

}