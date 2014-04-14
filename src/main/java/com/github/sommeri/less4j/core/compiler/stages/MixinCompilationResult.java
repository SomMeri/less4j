package com.github.sommeri.less4j.core.compiler.stages;

import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.compiler.expressions.GuardValue;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class MixinCompilationResult {

  private ReusableStructure mixin;
  private List<ASTCssNode> replacement;
  private IScope returnValues;
  private GuardValue guardValue;

  public MixinCompilationResult(ReusableStructure mixin, List<ASTCssNode> replacement, IScope returnValues) {
    this.mixin = mixin;
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

  public ReusableStructure getMixin() {
    return mixin;
  }

  public void setMixin(ReusableStructure mixin) {
    this.mixin = mixin;
  }

  @Override
  protected MixinCompilationResult clone() {
    return new MixinCompilationResult(mixin.clone(), ArraysUtils.deeplyClonedList(replacement), returnValues);
  }

}