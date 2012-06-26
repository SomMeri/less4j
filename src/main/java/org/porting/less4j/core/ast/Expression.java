package org.porting.less4j.core.ast;

import org.antlr.runtime.tree.CommonTree;

public abstract class Expression extends ASTCssNode {

  public Expression(CommonTree token) {
    super(token);
  }

}
