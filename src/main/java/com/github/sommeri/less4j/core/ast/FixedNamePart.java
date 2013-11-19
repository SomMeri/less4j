package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class FixedNamePart extends InterpolableNamePart {

  private String name;

  public FixedNamePart(HiddenTokenAwareTree token, String name) {
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
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.FIXED_NAME_PART;
  }

  public FixedNamePart clone() {
    return (FixedNamePart) super.clone();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
//    builder.append("FixedNamePart [name=");
    builder.append(name);
//    builder.append("]");
    return builder.toString();
  }
  
}
