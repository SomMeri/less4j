package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class SelectorCombinator extends ASTCssNode implements Cloneable {

  private CombinatorType combinator;
  private String symbol;

  public SelectorCombinator(HiddenTokenAwareTree underlyingStructure) {
    this(underlyingStructure, CombinatorType.DESCENDANT, CombinatorType.DESCENDANT.getDefaultSymbol());
  }

  public SelectorCombinator(HiddenTokenAwareTree underlyingStructure, CombinatorType combinator, String symbol) {
    super(underlyingStructure);
    this.combinator = combinator;
    this.symbol = symbol;
  }

  public CombinatorType getCombinatorType() {
    return combinator;
  }

  public void setCombinator(CombinatorType combinator) {
    this.combinator = combinator;
  }

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.SELECTOR_COMBINATOR;
  }

  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

  public enum CombinatorType {
    ADJACENT_SIBLING("+"), CHILD(">"), DESCENDANT("' '"), GENERAL_SIBLING("~"), HAT("^"), CAT("^^"), NAMED(null);
    
    private final String symbol;

    private CombinatorType(String symbol) {
      this.symbol = symbol;
    }

    private String getDefaultSymbol() {
      return symbol;
    }
  }

  @Override
  public SelectorCombinator clone() {
    return (SelectorCombinator) super.clone();
  }

  @Override
  public String toString() {
    return symbol;
  }
  
}
