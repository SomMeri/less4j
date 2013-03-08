package com.github.sommeri.less4j.core.problems;

import java.util.List;

import com.github.sommeri.less4j.LessCompiler.Problem;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.ArgumentDeclaration;
import com.github.sommeri.less4j.core.ast.Body;
import com.github.sommeri.less4j.core.ast.ComparisonExpressionOperator;
import com.github.sommeri.less4j.core.ast.EscapedSelector;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.Import;
import com.github.sommeri.less4j.core.ast.MediaQuery;
import com.github.sommeri.less4j.core.ast.MixinReference;
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

  public void lessjsDoesNotSupportPartiallyInterpolatedMediaQueries(MediaQuery mediaQuery) {
    collector.addWarning(new CompilationWarning(mediaQuery, "Usage of less4j only feature - input is incompatible with less.js. Variable can not be used to replace only part of media query, it must contain the whole query."));
  }

  public void wrongMemberInCssBody(ASTCssNode member, Body node) {
    ASTCssNode parent = node.getParent()==null? node : node.getParent();
    ASTCssNodeType parentType = node.getParent()==null? ASTCssNodeType.STYLE_SHEET : node.getParent().getType(); 
    collector.addWarning(new CompilationWarning(member, "Compilation resulted in incorrect CSS. The " + PrintUtils.toTypeName(member) + " ended up inside a body of " + PrintUtils.toTypeName(parentType) +" located at "+PrintUtils.toLocation(parent)+"."));
  }

  public void wrongMemberInLessBody(ASTCssNode member, Body node) {
    ASTCssNodeType parentType = node.getParent()==null? ASTCssNodeType.STYLE_SHEET : node.getParent().getType(); 
    collector.addError(new CompilationError(member, "The element " + PrintUtils.toTypeName(member) + " is not allowed to be a " + PrintUtils.toTypeName(parentType) +" member."));
  }

  public void notAColor(ASTCssNode node, String text) {
    collector.addError(new CompilationError(node, "The string \"" + text + "\" is not a valid color."));
  }

  public void warnMerginMediaQueryWithMedium(MediaQuery mediaQuery) {
    collector.addWarning(new CompilationWarning(mediaQuery, "Attempt to merge media query with a medium. Merge removed medium from inner media query, because the result CSS would be invalid otherwise."));
  }

  public void warnLessImportNoBaseDirectory(Expression urlExpression) {
    collector.addWarning(new CompilationWarning(urlExpression, "Attempt to import less file with an unknown compiled file location. Import statement left unchanged."));
  }

  public void errorImportedFileCanNotBeRead(Import node, String filename) {
    collector.addError(new CompilationError(node, "The file " + filename + " can not be read."));
  }

  public void errorImportedFileNotFound(Import node, String filename) {
    collector.addError(new CompilationError(node, "The file " + filename + " does not exist."));
  }

  public void errorWrongImport(Expression urlExpression) {
    collector.addError(new CompilationError(urlExpression, "Unsupported @import url kind. File link expression in @import can handle only strings and urls."));
  }

  public void nestedAppenderOnTopLevel(NestedSelectorAppender appender) {
    collector.addError(new CompilationError(appender, "Appender symbol is not allowed inside top level rulesets."));
  }

  public void interpolatedMixinReferenceSelector(MixinReference reference) {
    collector.addError(new CompilationError(reference.getFinalName(), "Interpolation is not allowed inside mixin references."));
  }

  public void extendedNamespaceReferenceSelector(MixinReference reference) {
    collector.addError(new CompilationError(reference, "Structures with extended names can not be used as namespaces."));
  }

  public void interpolatedNamespaceReferenceSelector(MixinReference reference) {
    collector.addError(new CompilationError(reference, "Interpolation is not allowed inside namespace references."));
  }

  public void wrongMemberBroughtIntoBody(ASTCssNode reference, ASTCssNode member, ASTCssNode body) {
    ASTCssNode parent = body.getParent()==null? body : body.getParent();
    collector.addError(new CompilationError(reference, "The reference brought " + PrintUtils.toTypeName(member) + " from " + PrintUtils.toLocation(member) + " into " + PrintUtils.toTypeName(parent) + " which started at " + PrintUtils.toLocation(body) + ". Compilation produced an incorrect CSS."));
  }

  public void errFormatWrongFirstParameter(Expression param) {
    collector.addError(new CompilationError(param, "First argument of format function must be either string or escaped value."));
  }

  public void wrongNumberOfArgumentsToFunction(Expression param, String function, int expectedArguments) {
    collector.addError(new CompilationError(param, "Wrong number of arguments to function '" + function + "', should be " + expectedArguments + "."));
  }

  public void wrongArgumentTypeToFunction(Expression param, String function, ASTCssNodeType received, ASTCssNodeType... expected) {
    collector.addError(new CompilationError(param, "Wrong argument type to function '" + function + "', expected " + PrintUtils.toTypeNames(expected) + " saw " + PrintUtils.toTypeName(received) + "."));
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
    return new CompilationError(reference, "Undefined parameter " + declaration.getVariable().getName() + " of mixin " + reference.getFinalNameAsString() + " defined on line " + mixin.getSourceLine());
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
    return createUndefinedMixin(reference.getFinalName(), reference);
  }

  private CompilationError createUndefinedMixin(ReusableStructureName name, MixinReference reference) {
    return new CompilationError(reference.getFinalName(), "The mixin \"" + name.asString() + "\" was not declared.");
  }

  public void unmatchedMixin(MixinReference reference) {
    collector.addError(createUnmatchedMixin(reference));
  }

  private CompilationError createUnmatchedMixin(MixinReference reference) {
    return createUnmatchedMixin(reference.getFinalName(), reference);
  }

  private CompilationError createUnmatchedMixin(ReusableStructureName name, MixinReference reference) {
    return new CompilationError(reference.getFinalName(), "The mixin \"" + name.asString() + "\" was not matched.");
  }

  public void undefinedNamespace(MixinReference reference) {
    collector.addError(createUndefinedNamespace(reference));
  }

  private CompilationError createUndefinedNamespace(MixinReference reference) {
    return createUndefinedNamespace(printer.toString(reference), reference);
  }

  private CompilationError createUndefinedNamespace(String name, MixinReference reference) {
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

  public void mathFunctionParameterNotANumberWarn(String functionName, Expression errorNode) {
    collector.addWarning(new CompilationWarning(errorNode, "function '" + functionName + "' requires number as a parameter."));
  }

  public void cannotEvaluate(Expression errorNode) {
    collector.addError(new CompilationError(errorNode, "Unable to evaluate expression"));
  }

  public void incompatibleComparisonOperand(Expression errorNode, ComparisonExpressionOperator operator) {
    collector.addError(new CompilationError(errorNode, "The operator '" + operator + "' on non-numbers is not defined. The behaviour of less.js and less4j may/does differ. Avoid its use with non-numbers or use one of `istype(@arument)` functions to protect against mismatches: `when (isnumber(@var)) and (@var "+ operator +" ...)`. The operator is located at position " + PrintUtils.toLocation(operator) +"."));
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

  public void addErrors(List<Problem> errors) {
    collector.addErrors(errors);
  }

}
