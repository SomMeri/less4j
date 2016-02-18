package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.sommeri.less4j.EmbeddedLessGenerator;
import com.github.sommeri.less4j.EmbeddedScriptGenerator;
import com.github.sommeri.less4j.LessCompiler.Configuration;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.AnonymousExpression;
import com.github.sommeri.less4j.core.ast.BinaryExpression;
import com.github.sommeri.less4j.core.ast.BinaryExpressionOperator;
import com.github.sommeri.less4j.core.ast.BinaryExpressionOperator.Operator;
import com.github.sommeri.less4j.core.ast.ComparisonExpression;
import com.github.sommeri.less4j.core.ast.ComparisonExpressionOperator;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.DetachedRuleset;
import com.github.sommeri.less4j.core.ast.EmbeddedScript;
import com.github.sommeri.less4j.core.ast.EscapedValue;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FaultyExpression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.Guard;
import com.github.sommeri.less4j.core.ast.GuardBinary;
import com.github.sommeri.less4j.core.ast.GuardCondition;
import com.github.sommeri.less4j.core.ast.GuardNegated;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.ast.IndirectVariable;
import com.github.sommeri.less4j.core.ast.ListExpression;
import com.github.sommeri.less4j.core.ast.ListExpressionOperator;
import com.github.sommeri.less4j.core.ast.NamedExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.ParenthesesExpression;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.SignedExpression;
import com.github.sommeri.less4j.core.ast.SignedExpression.Sign;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.compiler.expressions.strings.StringInterpolator;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.NullScope;
import com.github.sommeri.less4j.core.compiler.scopes.ScopeFactory;
import com.github.sommeri.less4j.core.problems.BugHappened;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.utils.CssPrinter;
import com.github.sommeri.less4j.utils.InStringCssPrinter;

public class ExpressionEvaluator {

  private VariableCycleDetector cycleDetector = new VariableCycleDetector();
  private final IScope lazyScope;

  private final ProblemsHandler problemsHandler;

  private ArithmeticCalculator arithmeticCalculator;
  private ColorsCalculator colorsCalculator;
  private ExpressionComparator comparator = new GuardsComparator();
  private List<FunctionsPackage> functions = new ArrayList<FunctionsPackage>(10);
  private StringInterpolator stringInterpolator;
  private StringInterpolator embeddedScriptInterpolator;
  private EmbeddedScriptGenerator embeddedScripting;

  public ExpressionEvaluator(ProblemsHandler problemsHandler, Configuration configuration) {
    this(new NullScope(), problemsHandler, configuration);
  }

  public ExpressionEvaluator(IScope scope, ProblemsHandler problemsHandler, Configuration configuration) {
    super();
    this.lazyScope = scope == null ? new NullScope() : scope;
    this.problemsHandler = problemsHandler;
    arithmeticCalculator = new ArithmeticCalculator(problemsHandler);
    colorsCalculator = new ColorsCalculator(problemsHandler);
    embeddedScripting = configuration.getEmbeddedScriptGenerator() == null ? new EmbeddedLessGenerator() : configuration.getEmbeddedScriptGenerator();
    stringInterpolator = new StringInterpolator(problemsHandler);
    embeddedScriptInterpolator = new StringInterpolator(embeddedScripting, problemsHandler);

    functions.add(new CustomFunctions(problemsHandler, configuration.getCustomFunctions()));
    functions.add(new MathFunctions(problemsHandler));
    functions.add(new StringFunctions(problemsHandler));
    functions.add(new ColorFunctions(problemsHandler));
    functions.add(new EmbeddedScriptFunctions(problemsHandler));
    functions.add(new TypeFunctions(problemsHandler));
    functions.add(new MiscFunctions(problemsHandler, configuration));
  }

  protected void addFunctionsPack(FunctionsPackage pack) {
    functions.add(pack);
  }

  public List<Expression> evaluateAll(List<Expression> expressions) {
    List<Expression> values = new ArrayList<Expression>();
    for (Expression argument : expressions) {
      values.add(evaluate(argument));
    }

    return values;
  }

  public IScope evaluateValues(IScope scope) {
    IScope result = ScopeFactory.createDummyScope();
    result.addFilteredVariables(toEvaluationFilter(), scope);
    return result;
  }

  private ExpressionFilter toEvaluationFilter() {
    return new ExpressionFilter() {

      @Override
      public Expression apply(Expression input) {
        return evaluate(input);
      }

      @Override
      public boolean accepts(String name, Expression value) {
        return true;
      }
    };
  }

  public Expression evaluate(CssString input) {
    String value = stringInterpolator.replaceIn(input.getValue(), this, input.getUnderlyingStructure());
    return new CssString(input.getUnderlyingStructure(), value, input.getQuoteType());
  }

  public Expression evaluate(EscapedValue input) {
    String value = stringInterpolator.replaceIn(input.getValue(), this, input.getUnderlyingStructure());
    return new EscapedValue(input.getUnderlyingStructure(), value, input.getQuoteType());
  }

  public Expression evaluate(EmbeddedScript input) {
    String value = embeddedScriptInterpolator.replaceIn(input.getValue(), this, input.getUnderlyingStructure());
    return new EmbeddedScript(input.getUnderlyingStructure(), value);
  }

  public Expression evaluate(Variable input) {
    return evaluate(input, true);
  }

  public Expression evaluateIfPresent(Variable input) {
    return evaluate(input, false);
  }

  private Expression evaluate(Variable input, boolean failOnUndefined) {
    if (cycleDetector.wouldCycle(input)) {
      problemsHandler.variablesCycle(cycleDetector.getCycleFor(input));
      return new FaultyExpression(input);
    }

    Expression expression = lazyScope.getValue(input);
    if (expression == null) {
      return handleUndefinedVariable(input, failOnUndefined);
    }

    cycleDetector.enteringVariableValue(input);
    Expression result = evaluate(expression);
    cycleDetector.leftVariableValue();
    return result;
  }

  public Expression evaluate(IndirectVariable input) {
    Expression reference = evaluate(lazyScope.getValue(input));

    CssPrinter printer = new InStringCssPrinter();
    printer.append(reference);
    String realName = printer.toString();

    Variable directVariable = new Variable(input.getUnderlyingStructure(), "@" + realName);
    return evaluate(directVariable);
  }

  public Expression evaluate(Expression input) {
    switch (input.getType()) {
    case FUNCTION:
      return evaluate((FunctionExpression) input);

    case BINARY_EXPRESSION:
      return evaluate((BinaryExpression) input);

    case LIST_EXPRESSION:
      return evaluate((ListExpression) input);

    case INDIRECT_VARIABLE:
      return evaluate((IndirectVariable) input);

    case VARIABLE:
      return evaluate((Variable) input);

    case PARENTHESES_EXPRESSION:
      return evaluate(((ParenthesesExpression) input).getEnclosedExpression());

    case SIGNED_EXPRESSION:
      return evaluate((SignedExpression) input);

    case NAMED_EXPRESSION:
      return evaluate((NamedExpression) input);

    case STRING_EXPRESSION:
      return evaluate((CssString) input);

    case ESCAPED_VALUE:
      return evaluate((EscapedValue) input);

    case EMBEDDED_SCRIPT:
      return evaluate((EmbeddedScript) input);

    case DETACHED_RULESET:
      return evaluate((DetachedRuleset) input);

    case IDENTIFIER_EXPRESSION:
    case COLOR_EXPRESSION:
    case NUMBER:
    case FAULTY_EXPRESSION:
    case UNICODE_RANGE_EXPRESSION:
    case EMPTY_EXPRESSION:
    case ANONYMOUS:
      return input.clone();

    default:
      throw new BugHappened("Unknown expression type " + input.getType(), input);
    }
  }

  private boolean booleanEvalueate(Expression input) {
    if (input.getType() == ASTCssNodeType.COMPARISON_EXPRESSION)
      return booleanEvalueate((ComparisonExpression) input);

    ASTCssNode value = evaluate(input);
    if (value.getType() != ASTCssNodeType.IDENTIFIER_EXPRESSION)
      return false;

    // this comparison must be case sensitive!
    IdentifierExpression identifier = (IdentifierExpression) value;
    return "true".equals(identifier.getValue());
  }

  public boolean booleanEvalueate(ComparisonExpression input) {
    Expression leftE = evaluate(input.getLeft());
    Expression rightE = evaluate(input.getRight());

    ComparisonExpressionOperator operator = input.getOperator();
    if (operator.getOperator() == ComparisonExpressionOperator.Operator.OPEQ)
      return comparator.equal(leftE, rightE);

    if (leftE.getType() != ASTCssNodeType.NUMBER) {
      problemsHandler.incompatibleComparisonOperand(leftE, operator);
      return false;
    }

    if (rightE.getType() != ASTCssNodeType.NUMBER) {
      problemsHandler.incompatibleComparisonOperand(rightE, operator);
      return false;
    }

    return compareNumbers((NumberExpression) leftE, (NumberExpression) rightE, operator);
  }

  private boolean compareNumbers(NumberExpression leftE, NumberExpression rightE, ComparisonExpressionOperator operator) {
    if (!leftE.convertibleTo(rightE))
      return false;

    Double left = leftE.convertIfPossible(rightE.getSuffix()).getValueAsDouble();
    Double right = rightE.getValueAsDouble();

    switch (operator.getOperator()) {
    case GREATER:
      return left.compareTo(right) > 0;

    case GREATER_OR_EQUAL:
      return left.compareTo(right) >= 0;

    case LOWER_OR_EQUAL:
      return left.compareTo(right) <= 0;

    case LOWER:
      return left.compareTo(right) < 0;

    default:
      throw new BugHappened("Unexpected comparison operator", operator);
    }
  }

  public Expression evaluate(FunctionExpression input) {
    Expression evaluatedParameter = evaluate(input.getParameter());
    List<Expression> splitParameters = (evaluatedParameter.getType() == ASTCssNodeType.EMPTY_EXPRESSION) ? new ArrayList<Expression>() : evaluatedParameter.splitByComma();

    if (!input.isCssOnlyFunction()) {
      for (FunctionsPackage pack : functions) {
        if (pack.canEvaluate(input, splitParameters))
          return pack.evaluate(input, splitParameters, evaluatedParameter);
      }
    }

    UnknownFunction unknownFunction = new UnknownFunction();
    return unknownFunction.evaluate(splitParameters, problemsHandler, input, evaluatedParameter);
  }

  public Expression evaluate(ListExpression input) {
    List<Expression> evaluated = new ArrayList<Expression>();
    for (Expression expression : input.getExpressions()) {
      evaluated.add(evaluate(expression));
    }
    ListExpression result = new ListExpression(input.getUnderlyingStructure(), evaluated, input.getOperator().clone(), input.getScope());
    result.configureParentToAllChilds();
    return result;
  }

  public Expression evaluate(NamedExpression input) {
    NamedExpression result = new NamedExpression(input.getUnderlyingStructure(), input.getName(), evaluate(input.getExpression()), input.getScope());
    result.configureParentToAllChilds();
    return result;
  }

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

    problemsHandler.nonNumberNegation(input);
    return new FaultyExpression(input);
  }

  public Expression evaluate(BinaryExpression input) {
    Expression leftValue = evaluate(input.getLeft());
    Expression rightValue = evaluate(input.getRight());
    BinaryExpressionOperator operator = input.getOperator();
    if (leftValue.isFaulty() || rightValue.isFaulty())
      return new FaultyExpression(input);

    if (arithmeticCalculator.accepts(operator, leftValue, rightValue))
      return arithmeticCalculator.evalute(input, leftValue, rightValue);

    if (colorsCalculator.accepts(operator, leftValue, rightValue))
      return colorsCalculator.evalute(input, leftValue, rightValue);

    List<Expression> members = Arrays.asList(leftValue, new AnonymousExpression(operator.getUnderlyingStructure(), operator.getOperator().getSymbol()), rightValue);
    ListExpression result = new ListExpression(input.getUnderlyingStructure(), members, new ListExpressionOperator(input.getUnderlyingStructure(), ListExpressionOperator.Operator.EMPTY_OPERATOR), lazyScope);

    return result;
    // problemsHandler.cannotEvaluate(input);
    // return new FaultyExpression(input);
  }

  public boolean guardsSatisfied(ReusableStructure mixin) {
    return evaluate(mixin.getGuards());
  }

  public boolean guardsSatisfied(RuleSet ruleSet) {
    return evaluate(ruleSet.getGuards());
  }

  public boolean evaluate(List<Guard> guards) {
    if (guards == null || guards.isEmpty())
      return true;

    // list of guards does or
    for (Guard guard : guards) {
      if (evaluate(guard))
        return true;
    }

    return false;
  }

  public boolean evaluate(Guard guard) {
    switch (guard.getGuardType()) {
    case BINARY:
      return evaluate((GuardBinary) guard);
    case CONDITION:
      return evaluate((GuardCondition) guard);
    case NEGATED:
      return evaluate((GuardNegated) guard);
    }

    throw new BugHappened("Unexpected guard type (" + guard.getGuardType() + ")", guard);
  }

  private boolean evaluate(GuardBinary guard) {
    boolean left = evaluate(guard.getLeft());

    switch (guard.getOperator()) {
    case AND: {
      boolean result = !left ? false : evaluate(guard.getRight());
      return result;
    }
    case OR: {
      boolean result = left ? true : evaluate(guard.getRight());
      return result;
    }
    }

    throw new BugHappened("Unexpected guard logical operator (" + guard.getOperator() + ")", guard);
  }

  private boolean evaluate(GuardNegated guard) {
    boolean nestedGuardValue = evaluate(guard.getGuard());
    boolean result = guard.isNegated() ? !nestedGuardValue : nestedGuardValue;
    return result;
  }

  private boolean evaluate(GuardCondition guardCondition) {
    Expression condition = guardCondition.getCondition();
    boolean result = booleanEvalueate(condition);
    return result;
  }

  public boolean isRatioExpression(Expression expression) {
    if (!(expression instanceof BinaryExpression))
      return false;

    BinaryExpression composed = (BinaryExpression) expression;
    if (composed.getOperator().getOperator() != Operator.SOLIDUS) {
      return false;
    }

    if (composed.getLeft().getType() != ASTCssNodeType.NUMBER)
      return false;

    if (composed.getRight().getType() != ASTCssNodeType.NUMBER)
      return false;

    return true;
  }

  public Expression evaluate(DetachedRuleset input) {
    DetachedRuleset clone = input.clone();

    IScope owningScope = clone.getScope();
    if (owningScope == null)
      throw new BugHappened("Detached ruleset with unknown scope.", input);

    return clone;
  }

  private Expression handleUndefinedVariable(Variable variable, boolean failOnUndefined) {
    if (failOnUndefined) {
      problemsHandler.undefinedVariable(variable);
      return new FaultyExpression(variable);
    } else {
      return null;
    }
  }

}
