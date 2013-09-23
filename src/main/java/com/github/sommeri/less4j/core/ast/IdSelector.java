package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class IdSelector extends ElementSubsequent {

  private InterpolableName name;

  public IdSelector(HiddenTokenAwareTree token, InterpolableName name) {
    super(token);
    this.name = name;
  }

  public String getName() {
    return name.getName();
  }

  public String getFullName() {
    return "#" + getName();
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
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(name);
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.ID_SELECTOR;
  }

  @Override
  public IdSelector clone() {
    IdSelector clone = (IdSelector)super.clone();
    clone.name = name.clone();
    clone.configureParentToAllChilds();
    return clone;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("IdSelector [");
    builder.append(getFullName());
    builder.append("]");
    return builder.toString();
  }

  
}
