package com.github.sommeri.less4j.core.problems;

import java.util.List;

import com.github.sommeri.less4j.LessCompiler.Problem;
import com.github.sommeri.less4j.LessProblems;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.ArgumentDeclaration;
import com.github.sommeri.less4j.core.ast.Body;
import com.github.sommeri.less4j.core.ast.ComparisonExpressionOperator;
import com.github.sommeri.less4j.core.ast.DetachedRuleset;
import com.github.sommeri.less4j.core.ast.DetachedRulesetReference;
import com.github.sommeri.less4j.core.ast.EscapedSelector;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FaultyExpression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.Import;
import com.github.sommeri.less4j.core.ast.MediaQuery;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.NestedSelectorAppender;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.PseudoClass;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.ReusableStructureName;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.ast.SignedExpression;
import com.github.sommeri.less4j.core.ast.SupportsLogicalOperator;
import com.github.sommeri.less4j.core.ast.UnknownAtRule;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.LessPrinter;
import com.github.sommeri.less4j.utils.PrintUtils;

//TODO: this could benefit from some kind of dependency injection framework.
public class ProblemsHandler implements LessProblems {

  private ProblemsCollector collector = new ProblemsCollector();
  private LessPrinter printer = new LessPrinter();

  public void wrongMemberInCssBody(ASTCssNode member, Body node) {
    ASTCssNode parent = node.getParent()==null? node : node.getParent();
    ASTCssNodeType parentType = node.getParent()==null? ASTCssNodeType.STYLE_SHEET : node.getParent().getType(); 
    addWarning(member, "Compilation resulted in incorrect CSS. The " + PrintUtils.toTypeName(member) + " ended up inside a body of " + PrintUtils.toTypeName(parentType) +" located at "+PrintUtils.toLocation(parent)+".");
  }

  public void errWrongSupportsLogicalOperator(SupportsLogicalOperator node, String faultyOperator) {
    addError(node, "@supports at rule does not support '" + faultyOperator + "' as a binary logical operator. You can use only 'and' and 'or'.");
  }

  public void wrongMemberInLessBody(ASTCssNode member, Body node) {
    ASTCssNodeType parentType = node.getParent()==null? ASTCssNodeType.STYLE_SHEET : node.getParent().getType(); 
    addError(member, "The element " + PrintUtils.toTypeName(member) + " is not allowed to be a " + PrintUtils.toTypeName(parentType) +" member.");
  }

  public void notAColor(ASTCssNode node, String text) {
    addError(node, "The string \"" + text + "\" is not a valid color.");
  }

  public void warnLessjsIncompatibleSelectorAttributeValue(Expression value) {
    addWarning(value, "This works, but is incompatible with less.js. Only less.js compatible selector attribute values are string, number and identifier.");
  }

  public void warnMerginMediaQueryWithMedium(MediaQuery mediaQuery) {
    addWarning(mediaQuery, "Attempt to merge media query with a medium. Merge removed medium from inner media query, because the result CSS would be invalid otherwise.");
  }

  public void unknownImportOption(Import node, String text) {
    addError(node, "Unknown import option \"" + text);
  }

  public void warnInconsistentSupportsLogicalConditionOperators(SupportsLogicalOperator faulty, SupportsLogicalOperator masterOperator) {
    String faultySymbol = faulty.getOperator().getSymbol();
    String masterSymbol = masterOperator.getOperator().getSymbol();
    addWarning(faulty, "CSS specification does not allow mixing of 'and', 'or', and 'not' operators without a layer of parentheses. Operators '" + faultySymbol + "' at " + PrintUtils.toLocation(faulty) + "' and '" + masterSymbol + "' at " + PrintUtils.toLocation(masterOperator) + " are in the same layer of parentheses.");
  }

  public void warnLessImportNoBaseDirectory(Expression urlExpression) {
    addWarning(urlExpression, "Attempt to import less file with an unknown compiled file location. Import statement left unchanged.");
  }

  public void errorFileReferenceNoBaseDirectory(ASTCssNode node, String path) {
    addError(node, "Attempt to reference file with an unknown compiled file location. Relative path: " + path);
  }

  public void errorFileCanNotBeRead(ASTCssNode node, String filename) {
    addError(node, "The file " + filename + " can not be read.");
  }

  public void errorFileNotFound(ASTCssNode node, String filename) {
    addError(node, "The file " + filename + " does not exist.");
  }

  public void errorWrongImport(Expression urlExpression) {
    addError(urlExpression, "Unsupported @import url kind. File link expression in @import can handle only strings and urls.");
  }

  public void nestedAppenderOnTopLevel(NestedSelectorAppender appender) {
    addError(appender, "Appender symbol is not allowed inside top level rulesets.");
  }

  public void interpolatedMixinReferenceSelector(MixinReference reference) {
    addError(reference.getFinalName(), "Interpolation is not allowed inside mixin references.");
  }

  public void interpolatedNamespaceReferenceSelector(MixinReference reference) {
    addError(reference, "Interpolation is not allowed inside namespace references.");
  }

  public void wrongMemberBroughtIntoBody(ASTCssNode reference, ASTCssNode member, ASTCssNode body) {
    ASTCssNode parent = body.getParent()==null? body : body.getParent();
    addError(reference, "The reference brought " + PrintUtils.toTypeName(member) + " from " + PrintUtils.toLocation(member) + " into " + PrintUtils.toTypeName(parent) + " which started at " + PrintUtils.toLocation(body) + ". Compilation produced an incorrect CSS.");
  }

  public void errFormatWrongFirstParameter(Expression param) {
    addError(param, "First argument of format function must be either string or escaped value.");
  }

  public void wrongNumberOfArgumentsToFunction(Expression param, String function, int expectedArguments) {
    addError(param, "Wrong number of arguments to function '" + function + "', should be " + expectedArguments + ".");
  }

  public void wrongNumberOfArgumentsToFunctionMin(Expression param, String function, int expectedArguments) {
    addError(param, "Wrong number of arguments to function '" + function + "', should be at least " + expectedArguments + ".");
  }

  public void wrongNumberOfArgumentsToFunctionMax(Expression param, String function, int expectedArguments) {
    addError(param, "Wrong number of arguments to function '" + function + "', should be at maximum " + expectedArguments + ".");
  }

  public void wrongArgumentTypeToFunction(Expression param, String function, ASTCssNodeType received, ASTCssNodeType... expected) {
    addError(param, "Wrong argument type to function '" + function + "', expected " + PrintUtils.toTypeNames(expected) + " saw " + PrintUtils.toTypeName(received) + ".");
  }

  public void warnScriptingNotSupported(ASTCssNode call, String errorName) {
    addError(call, errorName + "are not supported. The problem can be solved using custom functions. Compilation resulted in incorrect CSS.");
  }


  public void variablesCycle(List<Variable> cycle) {
    addError(cycle.get(0), "Cyclic references among variables: " + printer.toVariablesString(cycle));
  }

  public void mixinsCycle(List<MixinReference> cycle) {
    addError(cycle.get(0), "Cyclic references among mixins: " + printer.toMixinReferencesString(cycle));
  }

  public void deprecatedImportOnce(Import errorNode) {
    addWarning(errorNode, "`@import-once <url>` have been deprecated. Use `@import (once) <url>` instead. Input file is less.js incompatible.");
  }

  public void deprecatedImportMultiple(Import errorNode) {
    addWarning(errorNode, "`@import-multiple <url>` have been deprecated. Use `@import (multiple) <url>` instead. Input file is less.js incompatible.");
  }

  public void deprecatedSyntaxEscapedSelector(EscapedSelector errorNode) {
    addWarning(errorNode, "Selector fragment (~" + errorNode.getQuoteType() + errorNode.getValue() + errorNode.getQuoteType() + ") uses deprecated (~\"escaped-selector\") syntax. Use selector interpolation @{variableName} instead.");
  }

  public void warnEscapeFunctionArgument(Expression errorNode) {
    addWarning(errorNode, "Escape function argument should be a string.");
  }

  public void warnExtendInsideExtend(Selector errorNode) {
    addWarning(errorNode, "Target selector of extend contains nested extend. Nested extend will be ignored. The behaviour of less.js and less4j differ on this type of input.");
  }

  public void warnEFunctionArgument(Expression errorNode) {
    addError(errorNode, "e function argument should be a string.");
  }

  public void variableAsPseudoclassParameter(PseudoClass errorNode) {
    addWarning(errorNode.getParameter(), "Variables as pseudo classes parameters have been deprecated. Use selector interpolation @{variableName} instead.");
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
    return new CompilationError(reference.getFinalName(), "Could not find mixin named \"" + name.asString() + "\".");
  }

  public void detachedRulesetNotfound(DetachedRulesetReference reference) {
    addError(reference, "Could not find detached ruleset for \"" + reference.getVariable().getName() + "\".");
  }

  public void wrongDetachedRulesetReference(DetachedRulesetReference reference, Expression value) {
    addError(reference, "Detached ruleset reference \"" + reference.getVariable().getName() + "\"  does not evaluate to detached ruleset. It resolved to expression defined at " + printer.toPosition(value));
  }

  public void noMixinHasRightParametersCountError(MixinReference reference) {
    collector.addError(createNoMixinHasRightParametersCountError(reference));
  }

  public void patternsInMatchingMixinsDoNotMatch(MixinReference reference) {
    collector.addError(createPatternsInMatchingMixinsDoNotMatch(reference.getFinalName(), reference)); 
  }

  private CompilationError createPatternsInMatchingMixinsDoNotMatch(ReusableStructureName name, MixinReference reference) {
    return new CompilationError(reference.getFinalName(), "No mixin named \"" + name.asString() + "\" has matching patterns.");
  }

  private CompilationError createNoMixinHasRightParametersCountError(MixinReference reference) {
    return createNoMixinHasRightParametersCountError(reference.getFinalName(), reference);
  }

  private CompilationError createNoMixinHasRightParametersCountError(ReusableStructureName name, MixinReference reference) {
    return new CompilationError(reference.getFinalName(), "No mixin named \"" + name.asString() + "\" has the right number of parameters.");
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

  public void nonNumberNegation(SignedExpression errorNode) {
    addError(errorNode, "Cannot negate non number.");
  }

  public void subtractOrDiveColorFromNumber(Expression errorNode) {
    addError(errorNode, "Can't subtract or divide a color from a number");
  }

  public void mathFunctionParameterNotANumberWarn(String functionName, Expression errorNode) {
    addWarning(errorNode, "function '" + functionName + "' requires number as a parameter.");
  }

  public void cannotEvaluate(Expression errorNode) {
    addError(errorNode, "Unable to evaluate expression");
  }

  public void incompatibleComparisonOperand(Expression errorNode, ComparisonExpressionOperator operator) {
    addError(errorNode, "The operator '" + operator + "' on non-numbers is not defined. The behaviour of less.js and less4j may/does differ. Avoid its use with non-numbers or use one of `istype(@arument)` functions to protect against mismatches: `when (isnumber(@var)) and (@var "+ operator +" ...)`. The operator is located at position " + PrintUtils.toLocation(operator) +".");
  }

  public void rulesetWithoutSelector(RuleSet errorNode) {
    addWarning(errorNode, "Ruleset without selector encountered.");
  }

  public void divisionByZero(NumberExpression errorNode) {
    addWarning(errorNode, "Division by zero.");
  }

  public void warnIE8UnsafeDataUri(FunctionExpression errorNode, String filename, int fileSizeInKB, int dataUriMaxKb) {
    addWarning(errorNode, "Skipped data-uri embedding of " + filename + " because its size ("+fileSizeInKB+"dKB) exceeds IE8-safe "+dataUriMaxKb+"dKB!");
  }

  public void ambiguousDefaultSet(MixinReference reference, List<ASTCssNode> possibleMixins) {
    addError(reference, "Ambiguous use of `default()` found when matching reference " + reference.getFinalName() +". Matched mixins using default are located at " + printer.toNodesPositions(possibleMixins));
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

  @Override
  public void addError(ASTCssNode errorNode, String description) {
    collector.addError(new CompilationError(errorNode, description));
  }

  public void addError(HiddenTokenAwareTree errorNode, String description) {
    collector.addError(new CompilationError(new FaultyExpression(errorNode), description));
  }

  @Override
  public void addWarning(ASTCssNode weirdNode, String description) {
    collector.addWarning(new CompilationWarning(weirdNode, description));
  }

  public void warnUnknowAtRule(UnknownAtRule unknown) {
    addWarning(unknown, "Unknown at-rule, resulting css may be incorrect. Unknown at-rules support in less.js is undocumented and may be changed in the future (altrough it is unlikely). If that happen, less4j implementation will change too.");
  }

  public void warnSourceMapLinkWithoutCssResultLocation(ASTCssNode less) {
    addWarning(less, "Cannot link source map. Css result location is not know and could not be deduced from input less source.");
  }

  public void errUnknownEncodingCharsetSourceMap(ASTCssNode nodeForErrorReport, String encodingCharset) {
    addError(nodeForErrorReport, "Source map link or data could not be created. Requested charset '" + encodingCharset+"' is not available.");
  }

  public void detachedRulesetCallWithoutParentheses(DetachedRulesetReference reference) {
    addError(reference, "Detached ruleset call is missing parentheses.");
  }

  public void warnDetachedRulesetAsMixinParamDefault(Expression detached) {
    addWarning(detached, "This works, but is incompatible with less.js. Less.js does not allow detached rulesets as default mixin parameter values.");
  }

  public void wrongDetachedRulesetLocation(DetachedRuleset detachedRuleset) {
    addError(detachedRuleset, "Detached ruleset is not allowed outside of variable declaration.");
  }

  public void stringInterpolationNotSupported(HiddenTokenAwareTree errorNode, Expression value) {
    addError(errorNode, "String interpolation does not requeted expression type. Requested expression was defined at " + printer.toPosition(value));
  }

  public void errorIncompatibleTypesCompared(FunctionExpression errorNode, String suffix1, String suffix2) {
    addError(errorNode, "Can not compare '" + suffix1 +"' with '" + suffix2 + "'."); 
  }

  @Override
  public String toString() {
    return collector.toString();
  }

}
