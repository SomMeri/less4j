package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class Name extends ASTCssNode {
  
  private String name;

  public Name(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  public Name(HiddenTokenAwareTree underlyingStructure, String name) {
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
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.NAME;
  }
  
  @Override
  public Name clone() {
    Name result = (Name) super.clone();
    return result;
  }

}
