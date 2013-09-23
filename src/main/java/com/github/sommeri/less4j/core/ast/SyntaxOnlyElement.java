package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;


/**
 * 
 * Serves only as a comment holder. We need this class to keep comments around curly braces and similar symbols.
 * 
 * Example: `@viewport /* 1 * / { /* 2 * /` 
 *
 */
public class SyntaxOnlyElement extends ASTCssNode {
  
  private String symbol = "";

  public SyntaxOnlyElement(HiddenTokenAwareTree underlyingStructure, String symbol) {
    super(underlyingStructure);
    this.symbol = symbol;
  }

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

  @Override
  public SyntaxOnlyElement clone() {
    return (SyntaxOnlyElement) super.clone();
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.SYNTAX_ONLY_ELEMENT;
  }
  
  public static SyntaxOnlyElement lbrace(HiddenTokenAwareTree underlyingStructure) {
    return new SyntaxOnlyElement(underlyingStructure, "{");
  }

  public static SyntaxOnlyElement rbrace(HiddenTokenAwareTree underlyingStructure) {
    return new SyntaxOnlyElement(underlyingStructure, "}");
  }

}
