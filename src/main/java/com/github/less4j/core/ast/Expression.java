package com.github.less4j.core.ast;

import com.github.less4j.core.parser.HiddenTokenAwareTree;

public abstract class Expression extends ASTCssNode {

  public Expression(HiddenTokenAwareTree token) {
    super(token);
  }

}
