package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class CssClass extends ElementSubsequent {

  private InterpolableName name;

  public CssClass(HiddenTokenAwareTree token, InterpolableName name) {
    super(token);
    this.name = name;
  }

  public String getName() {
    return name.getName();
  }

  public String getFullName() {
    return "." + getName();
  }

  @Override
  public boolean isInterpolated() {
    return name.isInterpolated();
  }

  @Override
  public void extendName(String extension) {
    name.extendName(extension);
  }
  
  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.CSS_CLASS;
  }

  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(name);
  }

  @Override
  public CssClass clone() {
    CssClass clone = (CssClass)super.clone();
    clone.name = name.clone();
    clone.configureParentToAllChilds();
    return clone;
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
