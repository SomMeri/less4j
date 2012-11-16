package com.github.sommeri.less4j.core.compiler;

import com.github.sommeri.less4j.core.TranslationException;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ArgumentDeclaration;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.NamespaceReference;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.Variable;

@SuppressWarnings("serial")
public class CompileException extends TranslationException {

  private ASTCssNode node;

  public CompileException(ASTCssNode node) {
    super();
    this.node = node;
  }

  public CompileException(String message, Throwable th, ASTCssNode node) {
    super(message, th);
    this.node = node;
  }

  public CompileException(Throwable th, ASTCssNode node) {
    super(th);
    this.node = node;
  }

  public CompileException(String message, ASTCssNode node) {
    super(message);
    this.node = node;
  }

  @Override
  public String getMessage() {
    return super.getMessage() + getPositionInformation();
  }

  @Override
  public boolean hasErrorPosition() {
    return node != null;
  }

  @Override
  public int getCharPositionInLine() {
    if (!hasErrorPosition())
      return -1;

    return node.getCharPositionInSourceLine();
  }

  @Override
  public int getLine() {
    if (!hasErrorPosition())
      return -1;

    return node.getSourceLine();
  }

  public static void throwUndefinedMixinParameterValue(ReusableStructure mixin, ArgumentDeclaration declaration, MixinReference reference) {
    throw createUndefinedMixinParameterValue(mixin, declaration, reference);
    
  }

  private static CompileException createUndefinedMixinParameterValue(ReusableStructure mixin, ArgumentDeclaration declaration, MixinReference reference) {
    return new CompileException("Undefined parameter " + declaration.getVariable().getName() + " of mixin "+ mixin.getName() +" defined on line " + mixin.getSourceLine(), reference);
  }

  public static void throwUndeclaredVariable(Variable variable) {
    throw createUndeclaredVariable(variable.getName(), variable);
  }

  public static CompileException createUndeclaredVariable(Variable variable) {
    return createUndeclaredVariable(variable.getName(), variable);
  }

  public static void throwUndeclaredVariable(String name, ASTCssNode ifErrorNode) {
    throw createUndeclaredVariable(name, ifErrorNode);
  }

  public static CompileException createUndeclaredVariable(String name, ASTCssNode variable) {
    return new CompileException("The variable \"" + name + "\" was not declared.", variable);
  }

  public static void throwUndeclaredMixin(Variable variable) {
    throw createUndeclaredVariable(variable.getName(), variable);
  }

  public static void throwUndeclaredMixin(String name, ASTCssNode ifErrorNode) {
    throw createUndeclaredVariable(name, ifErrorNode);
  }

  public static CompileException createUndeclaredMixin(MixinReference reference) {
    return createUndeclaredMixin(reference.getName(), reference);
  }

  public static CompileException createUndeclaredMixin(String name, MixinReference variable) {
    return new CompileException("The mixin \"" + name + "\" was not declared.", variable);
  }

  public static CompileException createUnmatchedMixin(MixinReference reference) {
    return createUnmatchedMixin(reference.getName(), reference);
  }

  public static CompileException createUnmatchedMixin(String name, MixinReference variable) {
    return new CompileException("The mixin \"" + name + "\" was not matched.", variable);
  }

  public static void throwUnknownNamespace(NamespaceReference reference) {
    throw new CompileException("The namespace \"" + reference + "\" was not found.", reference);
  }

  public static void warnNoMixinsMatch(MixinReference reference) {
    CompileException ex = createUndeclaredMixin(reference);
    System.out.println(ex.getMessage());
    //FIXME:  create real warning
    
  }
}
