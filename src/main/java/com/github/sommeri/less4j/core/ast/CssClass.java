package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class CssClass extends ASTCssNode {

  private String name;

  public CssClass(HiddenTokenAwareTree token, String name) {
    super(token);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String getFullName() {
    return "." + name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.CSS_CLASS;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

  @Override
  public CssClass clone() {
    return (CssClass) super.clone();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("CssClass [");
    builder.append(getFullName());
    builder.append("]");
    return builder.toString();
  }
  
  
}
