package com.github.sommeri.less4j.core.compiler.stages;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Body;

public class AstLogic {

  public static boolean hasOwnScope(ASTCssNode node) {
    return (node instanceof Body);
  }
  
}
