package org.porting.less4j.core.parser;


public abstract class TokenTypeSwitch<T> {
  
  public T switchOn(HiddenTokenAwareTree token) {
    int type = token.getType();
    
    if (type==LessLexer.RULESET) {
      return handleRuleSet(token);
    }

    if (type==LessLexer.NESTED_RULESET)
      return handleNestedRuleSet(token);

    if (type==LessLexer.CSS_CLASS) {
      return handleCssClass(token);
    }
    
    if (type==LessLexer.PSEUDO) {
      return handlePseudo(token);
    }
    
    if (type==LessLexer.EOF) {
      return handleEOF(token);
    }

    if (type==LessLexer.SELECTOR) {
      return handleSelector(token);
    }

    if (type==LessLexer.STYLE_SHEET) {
      return handleStyleSheet(token);
    }

    if (type==LessLexer.ATTRIBUTE) {
      return handleSelectorAttribute(token);
    }

    if (type==LessLexer.ID_SELECTOR) {
      return handleIdSelector(token);
    }

    if (type==LessLexer.CHARSET_DECLARATION) {
      return handleCharsetDeclaration(token);
    }

    if (type==LessLexer.FONT_FACE_SYM) {
      return handleFontFace(token);
    }

    if (type==LessLexer.DECLARATION) {
      return handleDeclaration(token);
    }

    if (type==LessLexer.BODY) {
      return handleRuleSetsBody(token);
    }

    if (type==LessLexer.EXPRESSION) {
      return handleExpression(token);
    }
    if (type==LessLexer.NTH) {
      return handleNth(token);
    }
    
    if (type==LessLexer.TERM)
      return handleTerm(token);

    if (type==LessLexer.MEDIA_SYM)
      return handleMedia(token);

    if (type==LessLexer.MEDIUM_DECLARATION)
      return handleMediumDeclaration(token);

    if (type==LessLexer.MEDIA_QUERY)
      return handleMediaQuery(token);

    if (type==LessLexer.MEDIUM_TYPE) {
      return handleMedium(token);
    }

    if (type==LessLexer.MEDIA_EXPRESSION)
      return handleMediaExpression(token);

    if (type==LessLexer.VARIABLE_DECLARATION)
      return handleVariableDeclaration(token);

    if (type==LessLexer.VARIABLE)
      return handleVariable(token);
    
    if (type==LessLexer.VARIABLE)
      return handleIndirectVariable(token);

    throw new IncorrectTreeException("Unexpected token type: " + type + " for " + token.getText(), token);
  }

  public T handleVariableDeclaration(HiddenTokenAwareTree token) {
    return null;
  }

  public T handleVariable(HiddenTokenAwareTree token) {
    return null;
  }

  public T handleIndirectVariable(HiddenTokenAwareTree token) {
    return null;
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

  public T handleRuleSetsBody(HiddenTokenAwareTree token) {
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

  public T handleStyleSheet(HiddenTokenAwareTree token) {
    return null;
  }

  public T handleSelector(HiddenTokenAwareTree token) {
    return null;
  }

  public T handleRuleSet(HiddenTokenAwareTree token) {
    return null;
  }
  
  public T handleNestedRuleSet(HiddenTokenAwareTree token) {
    return null;
  }
  
  public T handleEOF(HiddenTokenAwareTree token) {
    return null;
  }

}
