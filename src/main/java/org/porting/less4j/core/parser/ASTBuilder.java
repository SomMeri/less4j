package org.porting.less4j.core.parser;

import org.antlr.runtime.tree.CommonTree;
import org.porting.less4j.core.ast.ASTCssNode;
import org.porting.less4j.core.ast.StyleSheet;

public class ASTBuilder {

  public StyleSheet parse(CommonTree tree) {
    ASTBuilderSwitch builder = new ASTBuilderSwitch();
    ASTCssNode result = builder.switchOn(tree);
    return (StyleSheet) result;
  }

}


