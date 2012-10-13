package com.github.sommeri.less4j.core.compiler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.ComparisonExpression;
import com.github.sommeri.less4j.core.ast.ComparisonExpressionOperator;
import com.github.sommeri.less4j.core.ast.ComposedExpression;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.ExpressionOperator.Operator;
import com.github.sommeri.less4j.core.ast.ExpressionOperator;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.Guard;
import com.github.sommeri.less4j.core.ast.GuardCondition;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.ast.IndirectVariable;
import com.github.sommeri.less4j.core.ast.NamedExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.ParenthesesExpression;
import com.github.sommeri.less4j.core.ast.SignedExpression;
import com.github.sommeri.less4j.core.ast.SignedExpression.Sign;
import com.github.sommeri.less4j.core.ast.Variable;

public class ExpressionEvaluator {

  private final AbstractEngine calculatingEngine;
  private final AbstractEngine passiveEngine;
  private AbstractEngine activeEngine;

  public ExpressionEvaluator(ActiveScope activeScope) {
    calculatingEngine = new CalculatingExpressionEvaluator(activeScope);
    passiveEngine = new PassiveExpressionEvaluator();
    turnOnEvaluation();
  }

  private boolean isTurnedOn() {
    return activeEngine == calculatingEngine;
  }

  public void turnOffEvaluation() {
    activeEngine = passiveEngine;
  }

  public boolean turnOnEvaluation() {
    boolean previousState = isTurnedOn();
    activeEngine = calculatingEngine;
    return previousState;
  }

  public boolean evaluate(List<Guard> guards) {
    return activeEngine.evaluate(guards);
  }

  public Expression evaluate(Variable input) {
    return activeEngine.evaluate(input);
  }

  public Expression evaluate(IndirectVariable input) {
    return activeEngine.evaluate(input);
  }

  public Expression evaluate(Expression input) {
    return activeEngine.evaluate(input);
  }

  public Expression evaluate(FunctionExpression input) {
    return activeEngine.evaluate(input);
  }

  public Expression evaluate(SignedExpression input) {
    return activeEngine.evaluate(input);
  }

  public Expression evaluate(ComposedExpression input) {
    return activeEngine.evaluate(input);
  }

  public boolean isRatioExpression(Expression expression) {
    return activeEngine.isRatioExpression(expression);
  }

  public Expression joinAll(List<Expression> allArguments, ASTCssNode parent) {
    if (allArguments.isEmpty())
      return new IdentifierExpression(parent.getUnderlyingStructure(), "");
    
    Iterator<Expression> iterator = allArguments.iterator();
    Expression result = iterator.next();
    while (iterator.hasNext()) {
      result = new ComposedExpression(parent.getUnderlyingStructure(), result, new ExpressionOperator(parent.getUnderlyingStructure()), iterator.next());
    }

    return result;
  }

  public List<Expression> evaluateAll(List<Expression> expressions) {
    List<Expression> values = new ArrayList<Expression>();
    for (Expression argument : expressions) {
      values.add(evaluate(argument));
    }
    
    return values;
  }

}

abstract class AbstractEngine {

  public abstract Expression evaluate(Variable input);

  public abstract boolean evaluate(List<Guard> guards);

  public abstract Expression evaluate(Expression input);

  public abstract Expression evaluate(IndirectVariable input);

  public abstract Expression evaluate(FunctionExpression input);

  public abstract Expression evaluate(SignedExpression input);

  public abstract Expression evaluate(ComposedExpression input);

  public boolean isRatioExpression(Expression expression) {
    if (!(expression instanceof ComposedExpression))
      return false;

    ComposedExpression composed = (ComposedExpression) expression;
    if (composed.getOperator().getOperator() != Operator.SOLIDUS) {
      return false;
    }

    if (composed.getLeft().getType() != ASTCssNodeType.NUMBER)
      return false;

    if (composed.getRight().getType() != ASTCssNodeType.NUMBER)
      return false;

    return true;
  }

}

class CalculatingExpressionEvaluator extends AbstractEngine {
  private final ActiveScope variableScope;
  private ArithmeticOperator arithmeticEngine = new ArithmeticOperator();;

  public CalculatingExpressionEvaluator(ActiveScope variableScope) {
    super();
    this.variableScope = variableScope;
  }

  @Override
  public Expression evaluate(Variable input) {
    Expression value = variableScope.getDeclaredValue(input);
    return evaluate(value);
  }

  @Override
  public Expression evaluate(IndirectVariable input) {
    Expression value = variableScope.getDeclaredValue(input);
    CssString realName = convertToStringExpression(evaluate(value), input);
    return evaluate(variableScope.getDeclaredValue("@" + realName.getValue(), realName));
  }

  private CssString convertToStringExpression(Expression evaluate, Expression errorNode) {
    if (!(evaluate instanceof CssString))
      throw new CompileException("Variable indirection works only with string values.", errorNode);

    return (CssString) evaluate;
  }

  //FIXME: refactoring change to reusable expression type switch
  @Override
  public Expression evaluate(Expression input) {
    switch (input.getType()) {
    case FUNCTION:
      return evaluate((FunctionExpression) input);

    case COMPOSED_EXPRESSION:
      return evaluate((ComposedExpression) input);

    case INDIRECT_VARIABLE:
      return evaluate((IndirectVariable) input);

    case VARIABLE:
      return evaluate((Variable) input);

    case PARENTHESES_EXPRESSION:
      return evaluate(((ParenthesesExpression) input).getEnclosedExpression());

    case NEGATED_EXPRESSION:
      return evaluate((SignedExpression) input);

    case NAMED_EXPRESSION:
      return ((NamedExpression) input).getExpression();

      //the value is already there, nothing to evaluate
    case IDENTIFIER_EXPRESSION:
    case COLOR_EXPRESSION:
    case NUMBER:
    case STRING_EXPRESSION:
      return input;

    default:
      throw new CompileException("Unknown expression type", input);
    }
  }

  private boolean booleanEvalueate(Expression input) {
    if (input.getType() == ASTCssNodeType.COMPARISON_EXPRESSION)
      return booleanEvalueate((ComparisonExpression) input);

    ASTCssNode value = evaluate(input);
    if (value.getType() != ASTCssNodeType.IDENTIFIER_EXPRESSION)
      return false;

    //this comparison must be case sensitive! 
    IdentifierExpression identifier = (IdentifierExpression) value;
    return "true".equals(identifier.getValue());
  }

 //FIXME: add also identifiers comparison
  public boolean booleanEvalueate(ComparisonExpression input) {
    Expression leftE = evaluate(input.getLeft());
    Expression rightE = evaluate(input.getRight());

    if (leftE.getType() != ASTCssNodeType.NUMBER)
      throw new CompileException("The expression must evaluate to number (for now).", leftE);

    if (rightE.getType() != ASTCssNodeType.NUMBER)
      throw new CompileException("The expression must evaluate to number (for now).", rightE);

    Double left = ((NumberExpression)leftE).getValueAsDouble();
    Double right = ((NumberExpression)rightE).getValueAsDouble();

    ComparisonExpressionOperator.Operator operator = input.getOperator().getOperator();
    switch (operator) {
    case GREATER:
      return left.compareTo(right)>0;

    case GREATER_OR_EQUAL:
      return left.compareTo(right)>=0;

    case OPEQ:
      return left.compareTo(right)==0;
      
    case LOWER_OR_EQUAL:
      return left.compareTo(right)<=0;

    case LOWER:
      return left.compareTo(right)<0;

    default:
      throw new CompileException("Unexpected comparison operator", input.getOperator());
    }
  }

  @Override
  public Expression evaluate(FunctionExpression input) {
    return input;
  }

  @Override
  public Expression evaluate(SignedExpression input) {
    Expression evaluate = evaluate(input.getExpression());
    if (evaluate instanceof NumberExpression) {
      NumberExpression negation = ((NumberExpression) evaluate).clone();
      if (input.getSign() == Sign.PLUS)
        return negation;

      negation.negate();
      negation.setOriginalString(null);
      negation.setExpliciteSign(false);
      return negation;
    }

    throw new CompileException("The expression does not evaluate to number and can not be negated.", input);
  }

  @Override
  public Expression evaluate(ComposedExpression input) {
    Expression leftValue = evaluate(input.getLeft());
    Expression rightValue = evaluate(input.getRight());

    if (arithmeticEngine.accepts(input.getOperator()))
      return arithmeticEngine.evalute(input, leftValue, rightValue);

    return new ComposedExpression(input.getUnderlyingStructure(), leftValue, input.getOperator(), rightValue);
  }

  @Override
  public boolean evaluate(List<Guard> guards) {
    if (guards == null || guards.isEmpty())
      return true;

    //list of guards does or
    for (Guard guard : guards) {
      if (evaluate(guard))
        return true;
    }
    
    return false;
  }

  public boolean evaluate(Guard guard) {
    List<GuardCondition> conditions = guard.getConditions();
    if (conditions == null || conditions.isEmpty())
      return true;

    //guards can only do and
    for (GuardCondition condition : conditions) {
      if (!evaluate(condition))
        return false;
    }
    
    return true;
  }

  private boolean evaluate(GuardCondition guardCondition) {
    Expression condition = guardCondition.getCondition();
    boolean conditionStatus = booleanEvalueate(condition);

    return guardCondition.isNegated() ? !conditionStatus : conditionStatus;
  }

}

class PassiveExpressionEvaluator extends AbstractEngine {

  public Expression evaluate(Variable input) {
    return input;
  }

  public Expression evaluate(IndirectVariable input) {
    return input;
  }

  public Expression evaluate(Expression input) {
    return input;
  }

  public Expression evaluate(FunctionExpression input) {
    return input;
  }

  public Expression evaluate(SignedExpression input) {
    return input;
  }

  public Expression evaluate(ComposedExpression input) {
    return input;
  }

  @Override
  public boolean evaluate(List<Guard> guards) {
    return false;
  }

}