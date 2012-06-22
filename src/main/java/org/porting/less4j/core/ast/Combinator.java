package org.porting.less4j.core.ast;

import org.antlr.runtime.tree.CommonTree;
import org.porting.less4j.core.parser.LessLexer;

public enum Combinator {
  PLUS, GREATER, EMPTY;

  //TODO probably does not belong here
  public static Combinator convert(CommonTree token) {
    switch (token.getType()) {
    case LessLexer.PLUS:
      return PLUS;
    case LessLexer.GREATER:
      return GREATER;
    case LessLexer.EMPTY_COMBINATOR:
      return EMPTY;
    }
    
    throw new IllegalStateException("Unknown combinator type.");
  }
}
