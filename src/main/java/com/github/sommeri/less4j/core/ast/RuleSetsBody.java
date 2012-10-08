package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class RuleSetsBody extends Body<ASTCssNode> {

  public RuleSetsBody(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  public RuleSetsBody(HiddenTokenAwareTree underlyingStructure, List<ASTCssNode> declarations) {
    super(underlyingStructure, declarations);
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.DECLARATIONS_BODY;
  }

  public RuleSetsBody clone() {
    return (RuleSetsBody) super.clone();
  }
}
