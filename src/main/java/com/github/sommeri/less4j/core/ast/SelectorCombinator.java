package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class SelectorCombinator extends ASTCssNode implements Cloneable {

  private Combinator combinator;

  public SelectorCombinator(HiddenTokenAwareTree underlyingStructure) {
    this(underlyingStructure, Combinator.DESCENDANT);
  }

  public SelectorCombinator(HiddenTokenAwareTree underlyingStructure, Combinator combinator) {
    super(underlyingStructure);
    this.combinator = combinator;
  }

  public Combinator getCombinator() {
    return combinator;
  }

  public void setCombinator(Combinator combinator) {
    this.combinator = combinator;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.SELECTOR_COMBINATOR;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

  public enum Combinator {
    ADJACENT_SIBLING("+"), CHILD(">"), DESCENDANT("' '"), GENERAL_SIBLING("~");
    
    private final String symbol;

    private Combinator(String symbol) {
      this.symbol = symbol;
    }

    public String getSymbol() {
      return symbol;
    }
  }

  @Override
  public SelectorCombinator clone() {
    return (SelectorCombinator) super.clone();
  }

  @Override
  public String toString() {
    return "" + combinator.getSymbol();
  }
  
}
