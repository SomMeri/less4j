package com.github.sommeri.less4j.core.ast;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public abstract class SupportsCondition extends ASTCssNode {

  public SupportsCondition(HiddenTokenAwareTree token) {
    super(token);
  }

  @Override
  public SupportsCondition clone() {
    SupportsCondition result = (SupportsCondition) super.clone();
    return result;
  }

}
