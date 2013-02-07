package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public abstract class Expression extends ASTCssNode {

  public Expression(HiddenTokenAwareTree token) {
    super(token);
  }

  public List<Expression> splitByComma() {
    return Collections.singletonList(this);
  }
  
  @Override
  public Expression clone() {
    Expression clone = (Expression) super.clone();
    return clone;
  }

}
