package org.porting.less4j.core.parser;


public abstract class TokenTypeSwitch<T> {
  
  public T switchOn(HiddenTokenAwareTree token) {
    int type = token.getType();
    
    if (type==LessLexer.RULESET) {
      return postprocess(handleRuleSet(token));
    }

    if (type==LessLexer.CSS_CLASS) {
      return postprocess(handleCssClass(token));
    }
    
    if (type==LessLexer.PSEUDO) {
      return postprocess(handlePseudo(token));
    }
    
    if (type==LessLexer.EOF) {
      return postprocess(handleEOF(token));
    }

    if (type==LessLexer.SELECTOR) {
      return postprocess(handleSelector(token));
    }

    if (type==LessLexer.STYLE_SHEET) {
      return postprocess(handleStyleSheet(token));
    }

    if (type==LessLexer.ATTRIBUTE) {
      return postprocess(handleSelectorAttribute(token));
    }

    if (type==LessLexer.ID_SELECTOR) {
      return postprocess(handleIdSelector(token));
    }

    if (type==LessLexer.CHARSET_DECLARATION) {
      return postprocess(handleCharsetDeclaration(token));
    }

    if (type==LessLexer.FONT_FACE_SYM) {
      return postprocess(handleFontFace(token));
    }

    if (type==LessLexer.DECLARATION) {
      return postprocess(handleDeclaration(token));
    }

    if (type==LessLexer.BODY_OF_DECLARATIONS) {
      return postprocess(handleDeclarationsBody(token));
    }

    if (type==LessLexer.EXPRESSION) {
      return postprocess(handleExpression(token));
    }
    if (type==LessLexer.NTH) {
      return postprocess(handleNth(token));
    }
    
    if (type==LessLexer.TERM)
      return postprocess(handleTerm(token));

    if (type==LessLexer.MEDIA_SYM)
      return postprocess(handleMedia(token));

    if (type==LessLexer.MEDIUM_DECLARATION)
      return postprocess(handleMediumDeclaration(token));

    if (type==LessLexer.MEDIA_QUERY)
      return postprocess(handleMediaQuery(token));

    if (type==LessLexer.MEDIUM_TYPE)
      return postprocess(handleMedium(token));

    if (type==LessLexer.MEDIA_EXPRESSION)
      return postprocess(handleMediaExpression(token));

    throw new IllegalStateException("Unexpected token type: " + type + " for " + token.getText());
  }

  public T handleMediaExpression(HiddenTokenAwareTree token) {
    return null;
  }

  public T handleMedium(HiddenTokenAwareTree token) {
    return null;
  }

  public T handleNth(HiddenTokenAwareTree token) {
    return null;
  }

  public T handleDeclarationsBody(HiddenTokenAwareTree token) {
    return null;
  }

  public T handleMediumDeclaration(HiddenTokenAwareTree token) {
    return null;
  }

  public T handleMediaQuery(HiddenTokenAwareTree token) {
    return null;
  }

  public T handleMedia(HiddenTokenAwareTree token) {
    return null;
  }

  public T handleTerm(HiddenTokenAwareTree token) {
    return null;
  }

  public T handleExpression(HiddenTokenAwareTree token) {
    return null;
  }

  public T handleDeclaration(HiddenTokenAwareTree token) {
    return null;
  }

  public T handleFontFace(HiddenTokenAwareTree token) {
    return null;
  }

  public T handleCharsetDeclaration(HiddenTokenAwareTree token) {
    return null;
  }

  public T handleIdSelector(HiddenTokenAwareTree token) {
    return null;
  }

  public T handleSelectorAttribute(HiddenTokenAwareTree token) {
    return null;
  }

  public T handleSelectorOperator(HiddenTokenAwareTree token) {
    return null;
  }
  
  public T handlePseudo(HiddenTokenAwareTree token) {
    return null;
  }

  public T handleCssClass(HiddenTokenAwareTree token) {
    return null;
  }

  public <M extends T> M postprocess(M something) {
    return something;
  }

  public T handleStyleSheet(HiddenTokenAwareTree token) {
    return null;
  }

  public T handleSelector(HiddenTokenAwareTree token) {
    return null;
  }

  public T handleRuleSet(HiddenTokenAwareTree token) {
    return null;
  }
  
  public T handleEOF(HiddenTokenAwareTree token) {
    return null;
  }

}
