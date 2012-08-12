package org.porting.less4j.core.ast;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

public class SelectorCombinator extends ASTCssNode {

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

  public enum Combinator {
    ADJACENT_SIBLING, CHILD, DESCENDANT, GENERAL_SIBLING;
  }

}
