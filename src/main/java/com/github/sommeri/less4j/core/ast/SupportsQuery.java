package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class SupportsQuery extends SupportsCondition {

  private SyntaxOnlyElement openingParentheses;
  private SyntaxOnlyElement closingParentheses;
  private Declaration declaration;

  public SupportsQuery(HiddenTokenAwareTree underlyingStructure, SyntaxOnlyElement openingParentheses, SyntaxOnlyElement closingParentheses, Declaration declaration) {
    super(underlyingStructure);
    this.openingParentheses = openingParentheses;
    this.closingParentheses = closingParentheses;
    this.declaration = declaration;
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

  public Declaration getDeclaration() {
    return declaration;
  }

  public void setDeclaration(Declaration declaration) {
    this.declaration = declaration;
  }

  @Override
  public List<ASTCssNode> getChilds() {
    List<ASTCssNode> childs = ArraysUtils.asNonNullList(openingParentheses, declaration, closingParentheses);
    return childs;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.SUPPORTS_QUERY;
  }

  @Override
  public SupportsQuery clone() {
    SupportsQuery result = (SupportsQuery) super.clone();
    result.openingParentheses = openingParentheses==null? null : openingParentheses.clone();
    result.closingParentheses = closingParentheses==null? null : closingParentheses.clone();
    result.declaration = declaration==null? null : declaration.clone();
    
    result.configureParentToAllChilds();
    return result;
  }

}
