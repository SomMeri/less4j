package org.porting.less4j.core.ast;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

public class Selector extends ASTCssNode {
  
  private SimpleSelector head;
  private SelectorCombinator combinator;
  private Selector right;

  public Selector(HiddenTokenAwareTree token) {
    super(token);
  }
  
  public Selector(HiddenTokenAwareTree token, SimpleSelector head, SelectorCombinator combinator, Selector right) {
    super(token);
    this.head = head;
    this.combinator = combinator;
    this.right = right;
  }

  public boolean isCombined() {
    return combinator!=null;
  }
  
  public SelectorCombinator getCombinator() {
    return combinator;
  }

  public SimpleSelector getHead() {
    return head;
  }

  public Selector getRight() {
    return right;
  }

  public void setHead(SimpleSelector head) {
    this.head = head;
  }

  public void setCombinator(SelectorCombinator combinator) {
    this.combinator = combinator;
  }

  public void setRight(Selector right) {
    this.right = right;
  }

  public ASTCssNodeType getType() {
    return ASTCssNodeType.SELECTOR;
  }

}
