package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class SupportsConditionNegation extends SupportsCondition {
  
  private SyntaxOnlyElement negation;
  private SupportsCondition condition;

  public SupportsConditionNegation(HiddenTokenAwareTree token, SyntaxOnlyElement negation, SupportsCondition condition) {
    super(token);
    this.negation = negation;
    this.condition = condition;
  }

  public SyntaxOnlyElement getNegation() {
    return negation;
  }

  public void setNegation(SyntaxOnlyElement negation) {
    this.negation = negation;
  }

  public SupportsCondition getCondition() {
    return condition;
  }

  public void setCondition(SupportsCondition condition) {
    this.condition = condition;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    List<ASTCssNode> childs = ArraysUtils.asNonNullList(negation, condition);
    return childs;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.SUPPORTS_CONDITION_NEGATION;
  }

  @Override
  public SupportsConditionNegation clone() {
    SupportsConditionNegation result = (SupportsConditionNegation) super.clone();
    result.negation = negation==null? null : negation.clone();
    result.condition = condition==null? null : condition.clone();
    result.configureParentToAllChilds();
    return result;
  }

}
