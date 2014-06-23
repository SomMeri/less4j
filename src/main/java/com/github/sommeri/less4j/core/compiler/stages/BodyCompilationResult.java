package com.github.sommeri.less4j.core.compiler.stages;

import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.compiler.expressions.GuardValue;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class BodyCompilationResult {

  private ASTCssNode compiledBodyOwner;
  private List<ASTCssNode> replacement;
  private IScope returnValues;
  private GuardValue guardValue;

  public BodyCompilationResult(ASTCssNode compiledBodyOwner, List<ASTCssNode> replacement, IScope returnValues) {
    this.compiledBodyOwner = compiledBodyOwner;
    this.replacement = replacement;
    this.returnValues = returnValues;
  }

  public void setGuardValue(GuardValue guardValue) {
    this.guardValue = guardValue;
  }

  public GuardValue getGuardValue() {
    return guardValue;
  }

  public List<ASTCssNode> getReplacement() {
    return replacement;
  }

  public void setReplacement(List<ASTCssNode> replacement) {
    this.replacement = replacement;
  }

  public IScope getReturnValues() {
    return returnValues;
  }

  public void setReturnValues(IScope returnValues) {
    this.returnValues = returnValues;
  }

  public ASTCssNode getCompiledBodyOwner() {
    return compiledBodyOwner;
  }

  public void setCompiledBodyOwner(ASTCssNode compiledBodyOwner) {
    this.compiledBodyOwner = compiledBodyOwner;
  }

  @Override
  protected BodyCompilationResult clone() {
    return new BodyCompilationResult(compiledBodyOwner.clone(), ArraysUtils.deeplyClonedList(replacement), returnValues);
  }

}