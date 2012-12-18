package com.github.sommeri.less4j.core.ast;

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
}
