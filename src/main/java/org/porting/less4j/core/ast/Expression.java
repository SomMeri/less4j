package org.porting.less4j.core.ast;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

public abstract class Expression extends ASTCssNode {

  public Expression(HiddenTokenAwareTree token) {
    super(token);
  }

}
