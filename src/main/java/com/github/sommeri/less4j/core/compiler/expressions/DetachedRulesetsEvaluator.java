package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.LessCompiler.Configuration;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.BinaryExpression;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.EmbeddedScript;
import com.github.sommeri.less4j.core.ast.EscapedValue;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FaultyExpression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.IndirectVariable;
import com.github.sommeri.less4j.core.ast.ListExpression;
import com.github.sommeri.less4j.core.ast.NamedExpression;
import com.github.sommeri.less4j.core.ast.ParenthesesExpression;
import com.github.sommeri.less4j.core.ast.SignedExpression;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.compiler.scopes.FullExpressionDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.ScopeFactory;
import com.github.sommeri.less4j.core.problems.BugHappened;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class DetachedRulesetsEvaluator {

  private VariableCycleDetector cycleDetector = new VariableCycleDetector();
  private final IScope scope;
  private final ProblemsHandler problemsHandler;
  private final ExpressionEvaluator expressionEvaluator;
  private List<FunctionsPackage> functions = new ArrayList<FunctionsPackage>();
  
  public DetachedRulesetsEvaluator(ProblemsHandler problemsHandler, Configuration configuration) {
    this(new NullScope(), problemsHandler, configuration);
  }

  public DetachedRulesetsEvaluator(IScope scope, ProblemsHandler problemsHandler, Configuration configuration) {
    super();
    this.scope = scope == null ? new NullScope() : scope;
    this.problemsHandler = problemsHandler;
    this.expressionEvaluator = new ExpressionEvaluator(scope, problemsHandler, configuration);
    
    functions.add(new CustomFunctions(problemsHandler, configuration.getCustomFunctions()));
    functions.add(new MathFunctions(problemsHandler));
    functions.add(new StringFunctions(problemsHandler));
    functions.add(new ColorFunctions(problemsHandler));
    functions.add(new MiscFunctions(problemsHandler));
    functions.add(new EmbeddedScriptFunctions(problemsHandler));
    functions.add(new TypeFunctions(problemsHandler));
  }
  
  protected void addFunctionsPack(FunctionsPackage pack){
    functions.add(pack);
  }

  public FullExpressionDefinition evaluate(CssString input, IScope owningScope) {
    return new FullExpressionDefinition(expressionEvaluator.evaluate(input), owningScope);
  }
  
  public FullExpressionDefinition evaluate(EscapedValue input, IScope owningScope) {
    return new FullExpressionDefinition(expressionEvaluator.evaluate(input), owningScope);
  }

  public FullExpressionDefinition evaluate(EmbeddedScript input, IScope owningScope) {
    return new FullExpressionDefinition(expressionEvaluator.evaluate(input), owningScope);
  }

  public FullExpressionDefinition evaluate(Variable input) {
    System.out.println("Evaluating: " + input.getName());
    if (cycleDetector.wouldCycle(input)) {
      problemsHandler.variablesCycle(cycleDetector.getCycleFor(input));
      return createFaultyExpression(input);
    }
      
    FullExpressionDefinition value = scope.getValue(input);
    if (value == null || value.getNode()==null) {
      problemsHandler.undefinedVariable(input);
      return createFaultyExpression(input);
    }
//    if (!(value.getNode() instanceof Expression)) {
//      //FIXME !!!!!!!!!!!!!!!!! what to do here????
//      return createFaultyExpression(input);
//    }
    
    if (value.getNode().getType()==ASTCssNodeType.DETACHED_RULESET && value.getOwningScope()==null) {
      System.out.println("wtf");
      System.out.println("wtf");
    }

    
    cycleDetector.enteringVariableValue(input);
    FullExpressionDefinition result = evaluate(value);
    cycleDetector.leftVariableValue();
    
    IScope owningScope = value.getOwningScope();
    if (result.getOwningScope()!=null) {
      //FIXME!!!!!!!!!!! chech whether joining needs to happen - e.g. whehter they see each other
      owningScope =  ScopeFactory.createJoinedScopesView(owningScope, result.getOwningScope());
    }
    return new FullExpressionDefinition(result.getNode(), owningScope);
  }

  private FullExpressionDefinition createFaultyExpression(Variable input) {
    return new FullExpressionDefinition(new FaultyExpression(input), null);
  }

  public Expression evaluateIfPresent(Variable input) {
    FullExpressionDefinition value = scope.getValue(input);
    if (value == null) {
      return null;
    }

    if (!(value.getNode() instanceof Expression)) {
      //FIXME !!!!!!!!!!!!!!!!! what to do here????
      return input;
    }
  //FIXME !!!!!!!!!!!!!!!!! add to declaration scopes
    return evaluate(value);
  }

  public FullExpressionDefinition evaluate(IndirectVariable input, IScope owningScope) {
    FullExpressionDefinition value = scope.getValue(input);
    if (!(value.getNode() instanceof Expression)) {
      //FIXME !!!!!!!!!!!!!!!!! what to do here????
      return createFaultyExpression(input);
    }
    Expression expression = value.getNode();
    if (!(expression instanceof CssString)) {
      problemsHandler.nonStringIndirection(input);
      return createFaultyExpression(input);
    }
    //FIXME !!!!!!!!!!!!!!!!! add to declaration scopes

    CssString realName = (CssString) expression;
    String realVariableName = "@" + realName.getValue();
    //FIXME: !!!!!!!!!!!! bypassing cycle detector!!!!!! (test also less.js)
    value = scope.getValue(realVariableName);
    if (value == null) {
      problemsHandler.undefinedVariable(realVariableName, realName);
      return new FullExpressionDefinition(new FaultyExpression(realName.getUnderlyingStructure()), null);
    }
    //FIXME !!!!!!!!!!!!!!!!! reusing variable ugly
    //FIXME !!!!!!!!!!!!!!!!! test less.js on scope accessibility
    //FIXME !!!!!!!!!!!!!!!!! cycling protections, shouldnt this reuse evaluate variable???
    return evaluate(value);
  }

  public FullExpressionDefinition evaluate(FullExpressionDefinition fullInput) {
    IScope owningScope = fullInput.getOwningScope();
    Expression input = fullInput.getNode();
    
    //FIXME!!!!!!!!!!!!!!!! ugly and maybe breakes scoping
    while (input.getType()==null) {
      FullExpressionDefinition fed = (FullExpressionDefinition) input;
      input = fed.getNode();
    }
    
    switch (input.getType()) {
    case FUNCTION:
      return evaluate((FunctionExpression) input, owningScope);

    case BINARY_EXPRESSION:
      return evaluate((BinaryExpression) input, owningScope);

    case LIST_EXPRESSION:
      return evaluate((ListExpression) input, owningScope);

    case INDIRECT_VARIABLE:
      return evaluate((IndirectVariable) input, owningScope);

    case VARIABLE:
      return evaluate((Variable) input);

    case PARENTHESES_EXPRESSION:
      return evaluate(toFullExpression(((ParenthesesExpression) input).getEnclosedExpression(), owningScope));

    case SIGNED_EXPRESSION:
      return evaluate((SignedExpression) input, owningScope);

    case NAMED_EXPRESSION:
      return evaluate((NamedExpression) input, owningScope);
      
    case STRING_EXPRESSION:
      return evaluate((CssString) input, owningScope);

    case ESCAPED_VALUE:
      return evaluate((EscapedValue) input, owningScope);

    case EMBEDDED_SCRIPT:
      return evaluate((EmbeddedScript) input, owningScope);

    //the value is already there, nothing to evaluate -- TODO - probably bug, should create clone()
    case DETACHED_RULESET:
    case IDENTIFIER_EXPRESSION:
    case COLOR_EXPRESSION:
    case NUMBER:
    case FAULTY_EXPRESSION:
    case UNICODE_RANGE_EXPRESSION:
    case EMPTY_EXPRESSION:
      return toFullExpression(input, owningScope);

    default:
      throw new BugHappened("Unknown expression type "+input.getType(), input);
    }
  }

  public FullExpressionDefinition evaluate(FunctionExpression input, IScope owningScope) {
    FullExpressionDefinition evaluatedParameter = evaluate(toFullExpression(input.getParameter(), owningScope));
    
    //FIXME: !!!!!!!!!!!!!!! the unbundling can break - what if the above returned real value instead of dummy one???? - maybe switch order, split and then evaluate each one separately
    Expression unbundledParameter = evaluatedParameter.getNode();
    List<Expression> splitParameters = (unbundledParameter.getType()==ASTCssNodeType.EMPTY_EXPRESSION)?new ArrayList<Expression>() : unbundledParameter.splitByComma();
    //FIXME: !!!!!!!!!!!!!!! tailored to extract function. need to create nicer solution that would work with anything
    List<Expression> unbundledSplitParameters = new ArrayList<Expression>();
    for (Expression expression : splitParameters) {
      if (expression instanceof FullExpressionDefinition) {
        FullExpressionDefinition new_name = (FullExpressionDefinition) expression;
        unbundledSplitParameters.add(new_name.getNode());
      } else {
        unbundledSplitParameters.add(expression);
      }
    }
    
    //FIXME: !!!!!!! not sure what to do here, if I do not want to break functions, I will have to unbundle expressions before sending them to functions
    if (!input.isCssOnlyFunction()) { 
      for (FunctionsPackage pack : functions) {
        if (pack.canEvaluate(input, unbundledSplitParameters))
          return toFullExpression(pack.evaluate(input, unbundledSplitParameters, evaluatedParameter), owningScope);
      }
    }
    
    UnknownFunction unknownFunction = new UnknownFunction();
    return toFullExpression(unknownFunction.evaluate(splitParameters, problemsHandler, input, evaluatedParameter), owningScope);
  }

  public FullExpressionDefinition evaluate(ListExpression input, IScope owningScope) {
    System.out.println("Evaluating list");
    List<Expression> evaluated = new ArrayList<Expression>();
    for (Expression expression : input.getExpressions()) {
      System.out.print("  list member " + expression);
      evaluated.add(evaluate(toFullExpression(expression, owningScope)));
      System.out.println("  -> " + ArraysUtils.last(evaluated));
    }
    ListExpression result = new ListExpression(input.getUnderlyingStructure(), evaluated, input.getOperator().clone());
    return new FullExpressionDefinition(result, owningScope);
  }

  public FullExpressionDefinition evaluate(NamedExpression input, IScope owningScope) {
    Expression expression = input.getExpression();
    FullExpressionDefinition fullExpression = toFullExpression(expression, owningScope);
    
    Expression result = new NamedExpression(input.getUnderlyingStructure(), input.getName(), evaluate(fullExpression));
    return new FullExpressionDefinition(result, owningScope);
  }

  private FullExpressionDefinition toFullExpression(Expression expression, IScope owningScope) {
    FullExpressionDefinition fullExpression = new FullExpressionDefinition(expression, owningScope);
    if (expression instanceof FullExpressionDefinition) {
      fullExpression = (FullExpressionDefinition) expression;
    }
    return fullExpression;
  }

  public FullExpressionDefinition evaluate(SignedExpression input, IScope owningScope) {
    return new FullExpressionDefinition(expressionEvaluator.evaluate(input), owningScope);
  }

  public FullExpressionDefinition evaluate(BinaryExpression input, IScope owningScope) {
    return new FullExpressionDefinition(expressionEvaluator.evaluate(input), owningScope);
  }

}
