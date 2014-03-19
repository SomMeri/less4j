package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.LessFunction;
import com.github.sommeri.less4j.LessProblems;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FaultyExpression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class CustomFunctions implements FunctionsPackage {

  private final ProblemsHandler problemsHandler;
  private List<LessFunction> functions;

  public CustomFunctions(ProblemsHandler problemsHandler, List<LessFunction> functions) {
    this.problemsHandler = problemsHandler;
    this.functions = functions == null ? new ArrayList<LessFunction>() : functions;
  }

  @Override
  public boolean canEvaluate(FunctionExpression input, List<Expression> parameters) {
    for (LessFunction function : functions) {
      if (function.canEvaluate(input, parameters))
        return true;
    }
    return false;
  }

  @Override
  public Expression evaluate(FunctionExpression input, List<Expression> parameters, Expression evaluatedParameter) {
    for (LessFunction function : functions) {
      if (function.canEvaluate(input, parameters)) {
        FunctionExpression inputClone = input.clone();
        List<Expression> parametersClone = ArraysUtils.deeplyClonedList(parameters);
        Expression evaluatedParameterClone = evaluatedParameter.clone();
        return fixResult(function.evaluate(inputClone, parametersClone, evaluatedParameterClone, new SafeLessProblem(problemsHandler, inputClone)), input);
      }
    }
    return new FaultyExpression(input);
  }

  private Expression fixResult(Expression result, FunctionExpression input) {
    if (result == null)
      return new FaultyExpression(input);

    fixNode(result, input);
    return result;
  }

  private void fixNode(ASTCssNode node, FunctionExpression input) {
    if (node.getUnderlyingStructure()==null)
      node.setUnderlyingStructure(input.getUnderlyingStructure());
    
    node.configureParentToAllChilds();
    for (ASTCssNode child : node.getChilds()) {
      fixNode(child, input);
    }
  }

  public class SafeLessProblem implements LessProblems {

    private static final String DEFAULT_DESCRIPTION = "<custom description was not specified>";
    private final ProblemsHandler problemsHandler;
    private final FunctionExpression defaultErrorNode;

    public SafeLessProblem(ProblemsHandler problemsHandler, FunctionExpression defaultErrorNode) {
      this.problemsHandler = problemsHandler;
      this.defaultErrorNode = defaultErrorNode;
    }

    @Override
    public void addError(ASTCssNode errorNode, String description) {
      errorNode = errorNode == null ? defaultErrorNode : errorNode;
      description = description == null ? DEFAULT_DESCRIPTION : description;

      problemsHandler.addError(errorNode, description);
    }

    @Override
    public void addWarning(ASTCssNode weirdNode, String description) {
      weirdNode = weirdNode == null ? defaultErrorNode : weirdNode;
      description = description == null ? DEFAULT_DESCRIPTION : description;

      problemsHandler.addWarning(weirdNode, description);
    }

  }

}
