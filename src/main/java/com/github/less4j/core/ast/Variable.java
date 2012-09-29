package com.github.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.less4j.core.parser.HiddenTokenAwareTree;

public class Variable extends Expression {
  
  private String name;

  public Variable(HiddenTokenAwareTree underlyingStructure, String name) {
    super(underlyingStructure);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.VARIABLE;
  }

}
