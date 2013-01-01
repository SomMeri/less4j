package com.github.sommeri.less4j.core.ast;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public abstract class SelectorPart extends ASTCssNode {

  public SelectorPart(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  @Override
  public SelectorPart clone() {
    return (SelectorPart) super.clone();
  }

  public boolean isClassesAndIdsOnlySelector() {
    return false;
  }

  public boolean isAppender() {
    return false;
  }

}
