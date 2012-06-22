package org.porting.less4j.core.parser;

import org.antlr.runtime.tree.CommonTree;
import org.porting.less4j.core.ast.SimpleSelector;
import org.porting.less4j.core.parser.LessLexer;

public abstract class TokenTypeSwitch<T> {
  
  public T switchOn(CommonTree token) {
    int type = token.getType();
    
    if (type==LessLexer.RULESET) {
      return safe(handleRuleSet(token));
    }

    if (type==LessLexer.CSS_CLASS) {
      return safe(handleCssClass(token));
    }
    
    if (type==LessLexer.PSEUDO) {
      return safe(handlePseudo(token));
    }
    
    if (type==LessLexer.EOF) {
      return safe(handleEOF(token));
    }

    if (type==LessLexer.SELECTOR) {
      return safe(handleSelector(token));
    }

    if (type==LessLexer.SIMPLE_SELECTOR) {
      return safe(handleSimpleSelector(token));
    }

    if (type==LessLexer.STYLE_SHEET) {
      return safe(handleStyleSheet(token));
    }

    throw new IllegalStateException("Unexpected token type: " + type);
  }

  public T handlePseudo(CommonTree token) {
    return null;
  }

  public T handleCssClass(CommonTree token) {
    return null;
  }

  @Deprecated
  private T safe(T something) {
    //only temporary measure, i want this to throw exception to find bugs fast
    //the real class should not have this
    something.toString();
    return something;
  }

  public T handleStyleSheet(CommonTree token) {
    return null;
  }

  public T handleSelector(CommonTree token) {
    return null;
  }

  public T handleRuleSet(CommonTree token) {
    return null;
  }
  
  public T handleEOF(CommonTree token) {
    return null;
  }

  public T handleSimpleSelector(CommonTree first) {
    return null;
  }

}
