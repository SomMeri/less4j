package com.github.sommeri.less4j.core.compiler.problems;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ArgumentDeclaration;
import com.github.sommeri.less4j.core.ast.ComparisonExpressionOperator;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.NamespaceReference;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.SignedExpression;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.utils.LessPrinter;

public class ProblemsHandler {
  
  private ProblemsCollector collector = new ProblemsCollector();
  private LessPrinter printer = new LessPrinter();
  
  public void undefinedMixinParameterValue(ReusableStructure mixin, ArgumentDeclaration declaration, MixinReference reference) {
    collector.addError(createUndefinedMixinParameterValue(mixin, declaration, reference));
  }

  private CompilationError createUndefinedMixinParameterValue(ReusableStructure mixin, ArgumentDeclaration declaration, MixinReference reference) {
    return new CompilationError(reference, "Undefined parameter " + declaration.getVariable().getName() + " of mixin "+ mixin.getName() +" defined on line " + mixin.getSourceLine());
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

  public void substractOrDiveColorFromNumber(Expression errorNode) {
    collector.addError(new CompilationError(errorNode, "Can't substract or divide a color from a number"));
  }

  public void cannotEvaluate(Expression errorNode) {
    collector.addError(new CompilationError(errorNode, "Unable to evaluate expression"));
  }

  public void incompatibleComparisonOperand(Expression errorNode, ComparisonExpressionOperator operator) {
    collector.addError(new CompilationError(errorNode, "The operator " + operator + " can be used only with numbers."));
    
  }
  
}

