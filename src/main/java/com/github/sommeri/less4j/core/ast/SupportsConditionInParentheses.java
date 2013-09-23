package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class SupportsConditionInParentheses extends SupportsCondition {

  private SyntaxOnlyElement openingParentheses;
  private SyntaxOnlyElement closingParentheses;
  private SupportsCondition condition;

  public SupportsConditionInParentheses(HiddenTokenAwareTree token, SyntaxOnlyElement openingParentheses, SupportsCondition condition, SyntaxOnlyElement closingParentheses) {
    super(token);
    this.openingParentheses = openingParentheses;
    this.closingParentheses = closingParentheses;
    this.condition = condition;
  }

  public SyntaxOnlyElement getOpeningParentheses() {
    return openingParentheses;
  }

  public void setOpeningParentheses(SyntaxOnlyElement openingParentheses) {
    this.openingParentheses = openingParentheses;
  }

  public SyntaxOnlyElement getClosingParentheses() {
    return closingParentheses;
  }

  public void setClosingParentheses(SyntaxOnlyElement closingParentheses) {
    this.closingParentheses = closingParentheses;
  }

  public SupportsCondition getCondition() {
    return condition;
  }

  public void setCondition(SupportsCondition condition) {
    this.condition = condition;
  }

  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    List<ASTCssNode> childs = ArraysUtils.asNonNullList(openingParentheses, condition, closingParentheses);
    return childs;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.SUPPORTS_CONDITION_PARENTHESES;
  }

  @Override
  public SupportsConditionInParentheses clone() {
    SupportsConditionInParentheses result = (SupportsConditionInParentheses) super.clone();
    result.openingParentheses = openingParentheses == null ? null : openingParentheses.clone();
    result.closingParentheses = closingParentheses == null ? null : closingParentheses.clone();
    result.condition = condition == null ? null : condition.clone();
    result.configureParentToAllChilds();
    return result;
  }

}
