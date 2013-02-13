package com.github.sommeri.less4j.core.ast;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public abstract class MediaExpression extends ASTCssNode {

  public MediaExpression(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  @Override
  public MediaExpression clone() {
    MediaExpression result = (MediaExpression) super.clone();
    return result;
  }

}
