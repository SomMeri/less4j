package org.porting.less4j.core.compiler;

import org.porting.less4j.core.ast.ComposedExpression;
import org.porting.less4j.core.ast.CssString;
import org.porting.less4j.core.ast.Expression;
import org.porting.less4j.core.ast.ExpressionOperator.Operator;
import org.porting.less4j.core.ast.ASTCssNodeType;
import org.porting.less4j.core.ast.FunctionExpression;
import org.porting.less4j.core.ast.IndirectVariable;
import org.porting.less4j.core.ast.NamedExpression;
import org.porting.less4j.core.ast.SignedExpression;
import org.porting.less4j.core.ast.NumberExpression;
import org.porting.less4j.core.ast.ParenthesesExpression;
import org.porting.less4j.core.ast.Variable;
import org.porting.less4j.core.ast.SignedExpression.Sign;

//FIXME and document variables evaluation is lazy. E.g. the declarations keeps only
//reference and not a value.
//
public class ExpressionEvaluator {

  private final ActiveVariableScope variableScope;
  private ArithmeticOperator arithmeticEngine = new ArithmeticOperator();;

  public ExpressionEvaluator(ActiveVariableScope variableScope) {
    super();
    this.variableScope = variableScope;
  }

  public Expression evaluate(Variable input) {
    Expression value = variableScope.getDeclaredValue(input);
    return evaluate(value);
  }

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

  public Expression evaluate(FunctionExpression input) {
    //FIXME not implemented yet
    System.out.println("Function: " + input.getName());
    return input;
    //throw new IllegalStateException("not implemented yet");
  }

  public Expression evaluate(SignedExpression input) {
    Expression evaluate = evaluate(input.getExpression());
    if (evaluate instanceof NumberExpression) {
      NumberExpression negation = (NumberExpression) evaluate;
      if (input.getSign() == Sign.PLUS)
        return negation;

      negation.negate();
      negation.setOriginalString(null); //technically, we should create a new object. The original string is not interesting anymore.
      negation.setExpliciteSign(false);
      return negation;
    }

    throw new CompileException("The expression does not evaluate to number and can not be negated.", input);
  }

  public Expression evaluate(ComposedExpression input) {
    System.out.println("Evaluating: " + input);
    Expression leftValue = evaluate(input.getLeft());
    Expression rightValue = evaluate(input.getRight());

    if (arithmeticEngine.accepts(input.getOperator()))
      return arithmeticEngine.evalute(input, leftValue, rightValue);

    return new ComposedExpression(input.getUnderlyingStructure(), leftValue, input.getOperator(), rightValue);
  }

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
