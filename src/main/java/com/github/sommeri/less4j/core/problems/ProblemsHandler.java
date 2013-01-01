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
import com.github.sommeri.less4j.core.ast.NestedSelectorAppender;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.PseudoClass;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.ReusableStructureName;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.SignedExpression;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.utils.LessPrinter;
import com.github.sommeri.less4j.utils.PrintUtils;

//TODO: this could benefit from some kind of dependency injection framework.
public class ProblemsHandler {

  private ProblemsCollector collector = new ProblemsCollector();
  private LessPrinter printer = new LessPrinter();

  public void nestedAppenderOnTopLevel(NestedSelectorAppender appender) {
    collector.addError(new CompilationError(appender, "Appender symbol is not allowed inside top level rulesets."));
  }

  public void interpolatedMixinReferenceSelector(MixinReference reference) {
    collector.addError(new CompilationError(reference, "Interpolation is not allowed inside mixin references."));
  }

  public void extendedNamespaceReferenceSelector(NamespaceReference reference) {
    collector.addError(new CompilationError(reference, "Structures with extended names can not be used as namespaces."));
  }

  public void interpolatedNamespaceReferenceSelector(NamespaceReference reference) {
    collector.addError(new CompilationError(reference, "Interpolation is not allowed inside namespace references."));
  }

  public void wrongMemberBroughtIntoBody(ASTCssNode reference, ASTCssNode member, ASTCssNode parent) {
    collector.addError(new CompilationError(reference, "The reference brought " + PrintUtils.toTypeName(member) + " from " + PrintUtils.toLocation(member) + " into " + PrintUtils.toTypeName(parent) + " which started at " + PrintUtils.toLocation(parent) + ". Compilation produced an incorrect CSS."));
  }

  public void unsupportedKeyframesMember(ASTCssNode errorNode) {
    collector.addError(new CompilationError(errorNode, "This element is not allowed to be @keyframes member."));
  }

  public void errFormatWrongFirstParameter(Expression param) {
    collector.addError(new CompilationError(param, "First argument of format function must be either string or escaped value."));
  }

  public void variablesCycle(List<Variable> cycle) {
    collector.addError(new CompilationError(cycle.get(0), "Cyclic references among variables: " + printer.toVariablesString(cycle)));
  }

  public void mixinsCycle(List<MixinReference> cycle) {
    collector.addError(new CompilationError(cycle.get(0), "Cyclic references among mixins: " + printer.toMixinReferencesString(cycle)));
  }

  public void deprecatedSyntaxEscapedSelector(EscapedSelector errorNode) {
    collector.addWarning(new CompilationWarning(errorNode, "Selector fragment (~" + errorNode.getQuoteType() + errorNode.getValue() + errorNode.getQuoteType() + ") uses deprecated (~\"escaped-selector\") syntax. Use selector interpolation @{variableName} instead."));
  }

  public void warnEscapeFunctionArgument(Expression errorNode) {
    collector.addWarning(new CompilationWarning(errorNode, "Escape function argument should be a string."));
  }

  public void warnEFunctionArgument(Expression errorNode) {
    collector.addError(new CompilationError(errorNode, "e function argument should be a string."));
  }

  public void variableAsPseudoclassParameter(PseudoClass errorNode) {
    collector.addWarning(new CompilationWarning(errorNode.getParameter(), "Variables as pseudo classes parameters have been deprecated. Use selector interpolation @{variableName} instead."));
  }

  public void undefinedMixinParameterValue(ReusableStructure mixin, ArgumentDeclaration declaration, MixinReference reference) {
    collector.addError(createUndefinedMixinParameterValue(mixin, declaration, reference));
  }

  private CompilationError createUndefinedMixinParameterValue(ReusableStructure mixin, ArgumentDeclaration declaration, MixinReference reference) {
    return new CompilationError(reference, "Undefined parameter " + declaration.getVariable().getName() + " of mixin " + reference.getNameAsString() + " defined on line " + mixin.getSourceLine());
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

  private CompilationError createUndefinedMixin(ReusableStructureName name, MixinReference reference) {
    return new CompilationError(reference, "The mixin \"" + name.asString() + "\" was not declared.");
  }

  public void unmatchedMixin(MixinReference reference) {
    collector.addError(createUnmatchedMixin(reference));
  }

  private CompilationError createUnmatchedMixin(MixinReference reference) {
    return createUnmatchedMixin(reference.getName(), reference);
  }

  private CompilationError createUnmatchedMixin(ReusableStructureName name, MixinReference reference) {
    return new CompilationError(reference, "The mixin \"" + name.asString() + "\" was not matched.");
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
    collector.addError(new CompilationError(errorNode, "function '" + functionName + "' requires number as a parameter."));
  }

  public void mathFunctionParameterNotANumberWarn(String functionName, Expression errorNode) {
    collector.addWarning(new CompilationWarning(errorNode, "function '" + functionName + "' requires number as a parameter."));
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
