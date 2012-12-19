package com.github.sommeri.less4j.core.problems;

import java.util.List;

import com.github.sommeri.less4j.LessCompiler.Problem;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ArgumentDeclaration;
import com.github.sommeri.less4j.core.ast.ComparisonExpressionOperator;
import com.github.sommeri.less4j.core.ast.EscapedSelector;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.NamespaceReference;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.PseudoClass;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.SignedExpression;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.utils.LessPrinter;

//TODO: this could benefit from some kind of dependency injection framework.
public class ProblemsHandler {
  
  private ProblemsCollector collector = new ProblemsCollector();
  private LessPrinter printer = new LessPrinter();
  
  public void composedMixinReferenceSelector(MixinReference reference) {
    collector.addError(new CompilationError(reference, "Mixin reference is composed of multiple parts."));
    
  }

  public void deprecatedSyntaxEscapedSelector(EscapedSelector errorNode) {
    collector.addWarning(new CompilationWarning(errorNode, "Selector fragment (~"+errorNode.getQuoteType()+errorNode.getValue()+errorNode.getQuoteType()+ ") uses deprecated (~\"escaped-selector\") syntax. Use selector interpolation @{variableName} instead."));
  }

  public void variableAsPseudoclassParameter(PseudoClass errorNode) {
    collector.addWarning(new CompilationWarning(errorNode.getParameter(), "Variables as pseudo classes parameters have been deprecated. Use selector interpolation @{variableName} instead."));
  }

  public void undefinedMixinParameterValue(ReusableStructure mixin, ArgumentDeclaration declaration, MixinReference reference) {
    collector.addError(createUndefinedMixinParameterValue(mixin, declaration, reference));
  }

  private CompilationError createUndefinedMixinParameterValue(ReusableStructure mixin, ArgumentDeclaration declaration, MixinReference reference) {
    return new CompilationError(reference, "Undefined parameter " + declaration.getVariable().getName() + " of mixin "+ reference.getName() +" defined on line " + mixin.getSourceLine());
  }

  public void undefinedVariable(Variable variable) {
    collector.addError(createUndefinedVariable(variable.getName(), variable));
  }

  public void undefinedVariable(String name, ASTCssNode ifErrorNode) {
    collector.addError(createUndefinedVariable(name, ifErrorNode));
  }

  @SuppressWarnings("unused")
  private CompilationError createUndefinedVariable(Variable variable) {
    return createUndefinedVariable(variable.getName(), variable);
  }

  private CompilationError createUndefinedVariable(String name, ASTCssNode variable) {
    return new CompilationError(variable, "The variable \"" + name + "\" was not declared.");
  }

  public void undefinedMixin(MixinReference reference) {
    collector.addError(createUndefinedMixin(reference));
  }

  private CompilationError createUndefinedMixin(MixinReference reference) {
    return createUndefinedMixin(reference.getName(), reference);
  }

  private CompilationError createUndefinedMixin(String name, MixinReference reference) {
    return new CompilationError(reference, "The mixin \"" + name + "\" was not declared.");
  }

  public void unmatchedMixin(MixinReference reference) {
    collector.addError(createUnmatchedMixin(reference));
  }

  private CompilationError createUnmatchedMixin(MixinReference reference) {
    return createUnmatchedMixin(reference.getName(), reference);
  }

  private CompilationError createUnmatchedMixin(String name, MixinReference reference) {
    return new CompilationError(reference, "The mixin \"" + name + "\" was not matched.");
  }

  public void undefinedNamespace(NamespaceReference reference) {
    collector.addError(createUndefinedNamespace(reference));
  }

  private CompilationError createUndefinedNamespace(NamespaceReference reference) {
    return createUndefinedNamespace(printer.toString(reference), reference);
  }

  private CompilationError createUndefinedNamespace(String name, NamespaceReference reference) {
    return new CompilationError(reference, "The namespace \"" + name + "\" was not declared.");
  }

  public void nonStringIndirection(Expression errorNode) {
    collector.addError(createNonStringIndirection(errorNode));
  }

  private CompilationError createNonStringIndirection(Expression errorNode) {
    return new CompilationError(errorNode, "Variable indirection works only with string values.");
  }
  
  public void nonNumberNegation(SignedExpression errorNode) {
    collector.addError(new CompilationError(errorNode, "Cannot negate non number."));
  }

  public void subtractOrDiveColorFromNumber(Expression errorNode) {
    collector.addError(new CompilationError(errorNode, "Can't subtract or divide a color from a number"));
  }

  public void mathFunctionParameterNotANumber(String functionName, Expression errorNode) {
    collector.addError(new CompilationError(errorNode, "function '" +functionName+ "' requires number as a parameter."));
  }

  public void mathFunctionParameterNotANumberWarn(String functionName, Expression errorNode) {
    collector.addWarning(new CompilationWarning(errorNode, "function '" +functionName+ "' requires number as a parameter."));
  }

  public void cannotEvaluate(Expression errorNode) {
    collector.addError(new CompilationError(errorNode, "Unable to evaluate expression"));
  }

  public void incompatibleComparisonOperand(Expression errorNode, ComparisonExpressionOperator operator) {
    collector.addError(new CompilationError(errorNode, "The operator '" + operator + "' can be used only with numbers."));
    
  }

  public void rulesetWithoutSelector(RuleSet errorNode) {
    collector.addWarning(new CompilationWarning(errorNode, "Ruleset without selector encountered."));
  }

  public void divisionByZero(NumberExpression errorNode) {
    collector.addWarning(new CompilationWarning(errorNode, "Division by zero."));
  }
  
  public boolean hasErrors() {
    return collector.hasErrors();
  }

  public boolean hasWarnings() {
    return collector.hasWarnings();
  }

  public List<Problem> getWarnings() {
    return collector.getWarnings();
  }

  public List<Problem> getErrors() {
    return collector.getErrors();
  }

}

