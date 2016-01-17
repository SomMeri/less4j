package com.github.sommeri.less4j.core.parser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.antlr.runtime.Token;

public class LexerLogic {

  private static final String KEYFRAMES = "keyframes";
  private static final String DOCUMENT = "document";
  private static final String VIEWPORT = "viewport";
  private static final String SUPPORTS = "supports";
  private static final String CHARSET = "charset";

  private static final Set<String> PAGE_MARGIN_BOXES = new HashSet<String>(Arrays.asList(new String[] { "@top-left-corner", "@top-left", "@top-center", "@top-right", "@top-right-corner", "@bottom-left-corner", "@bottom-left", "@bottom-center", "@bottom-right", "@bottom-right-corner", "@left-top", "@left-middle", "@left-bottom", "@right-top", "@right-middle", "@right-bottom" }));

  public int atNameType(String text) {
    if (text == null)
      return LessLexer.AT_NAME;

    if (text.toLowerCase().endsWith(DOCUMENT))
      return LessLexer.AT_DOCUMENT;

    if (text.toLowerCase().endsWith(KEYFRAMES))
      return LessLexer.AT_KEYFRAMES;

    if (text.toLowerCase().endsWith(VIEWPORT))
      return LessLexer.AT_VIEWPORT;

    if (text.toLowerCase().endsWith(SUPPORTS))
      return LessLexer.AT_SUPPORTS;

    if (text.toLowerCase().endsWith(CHARSET))
      return LessLexer.AT_CHARSET;

    return LessLexer.AT_NAME;
  }

  public boolean isAtName(int type) {
    return type == LessLexer.AT_NAME || type == LessLexer.AT_DOCUMENT || type == LessLexer.AT_KEYFRAMES || type == LessLexer.AT_VIEWPORT || type == LessLexer.AT_SUPPORTS || type == LessLexer.AT_CHARSET;
  }

  public boolean isIdentifier(int type) {
    return type == LessLexer.IDENT || type == LessLexer.IDENT_WHEN || type == LessLexer.IDENT_NOT || type == LessLexer.IDENT_AND || type == LessLexer.IDENT_OR|| type == LessLexer.IDENT_NTH_CHILD || type == LessLexer.NTH_LAST_CHILD || type == LessLexer.NTH_OF_TYPE || type == LessLexer.NTH_LAST_OF_TYPE || type == LessLexer.IDENT_EXTEND;
  }

  public boolean isPageMarginBox(Token token) {
    return isAmongAtNames(token, PAGE_MARGIN_BOXES);
  }

  private boolean isAmongAtNames(Token token, Set<String> atNames) {
    if (token.getType() != LessParser.AT_NAME || token.getText() == null)
      return false;

    String text = token.getText().toLowerCase();
    return atNames.contains(text);
  }

  public boolean truthy() {
    return true;
  }

}
