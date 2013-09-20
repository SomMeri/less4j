package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

//interpolable name part MUST be a child of interpolable name
public abstract class InterpolableNamePart extends ASTCssNode {

  public InterpolableNamePart(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  public abstract String getName();
  
  @Override
  public InterpolableName getParent() {
    return (InterpolableName) super.getParent();
  }
  
  public InterpolableNamePart clone() {
    return (InterpolableNamePart) super.clone();
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ASTCssNodeType getType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String toString() {
    return getName();
  }
  
  
}
