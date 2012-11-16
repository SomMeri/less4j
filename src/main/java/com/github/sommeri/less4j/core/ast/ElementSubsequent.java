package com.github.sommeri.less4j.core.ast;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public abstract class ElementSubsequent extends ASTCssNode {

  public ElementSubsequent(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  public abstract String getName();

  public abstract String getFullName();

  public abstract void setName(String name);

  public void extendName(String extension) {
    setName(getName() + extension);
  }
  
  @Override
  public ElementSubsequent clone() {
    ElementSubsequent result = (ElementSubsequent) super.clone();
    return result;
  }

}
