package com.github.sommeri.less4j.core.ast;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public abstract class Guard extends ASTCssNode {

  public Guard(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.GUARD;
  }

  public abstract Type getGuardType();
  
  public enum Type {
    NEGATED, BINARY, CONDITION
  }


  @Override
  public Guard clone() {
    Guard result = (Guard) super.clone();
    return result;
  }

}
