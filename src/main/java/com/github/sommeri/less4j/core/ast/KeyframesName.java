package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class KeyframesName extends ASTCssNode {

  private Expression name;

  public KeyframesName(HiddenTokenAwareTree underlyingStructure, Expression name) {
    super(underlyingStructure);
    this.name = name;
  }

  public Expression getName() {
    return name;
  }

  public void setName(Expression name) {
    this.name = name;
  }

  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    List<ASTCssNode> result = ArraysUtils.asNonNullList((ASTCssNode)name);
    return result;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.KEYFRAMES_NAME;
  }

  @Override
  public KeyframesName clone() {
    KeyframesName clone = (KeyframesName) super.clone();
    clone.name = name == null ? null : name.clone();
    clone.configureParentToAllChilds();
    return clone;
  }

}
