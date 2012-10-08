package com.github.sommeri.less4j.core.ast;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class IndirectVariable extends Variable {
  
  public IndirectVariable(HiddenTokenAwareTree underlyingStructure, String name) {
    super(underlyingStructure, name);
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.INDIRECT_VARIABLE;
  }

  @Override
  public IndirectVariable clone() {
    return (IndirectVariable) super.clone();
  }
}
