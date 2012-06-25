package org.porting.less4j.core.ast;

import org.antlr.runtime.tree.CommonTree;

//FIXME: make expression abstract and then make both expressions and terms its childs. This way the composed expresison 
//will be expression operator expression and not something operator something else. 
public abstract class Expression extends ASTCssNode {

  public Expression(CommonTree token) {
    super(token);
  }

}
