package com.github.sommeri.less4j.core.parser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;

public class ParsersSemanticPredicates {

  private static final String KEYFRAMES = "keyframes";
  private static final String DOCUMENT = "document";
  private static final String VIEWPORT = "viewport";
  private static final String SUPPORTS = "supports";
  private static final String CHARSET = "charset";

  private static final Set<String> PAGE_MARGIN_BOXES = new HashSet<String>(Arrays.asList(new String[] { "@top-left-corner", "@top-left", "@top-center", "@top-right", "@top-right-corner", "@bottom-left-corner", "@bottom-left", "@bottom-center", "@bottom-right", "@bottom-right-corner", "@left-top", "@left-middle", "@left-bottom", "@right-top", "@right-middle", "@right-bottom" }));

  private static Set<String> NTH_PSEUDOCLASSES = new HashSet<String>();
  static {
    NTH_PSEUDOCLASSES.add("nth-child");
    NTH_PSEUDOCLASSES.add("nth-last-child");
    NTH_PSEUDOCLASSES.add("nth-of-type");
    NTH_PSEUDOCLASSES.add("nth-last-of-type");
  }

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
    return type == LessLexer.IDENT || type == LessLexer.IDENT_WHEN || type == LessLexer.IDENT_NOT  || type == LessLexer.IDENT_NTH_CHILD || type == LessLexer.NTH_LAST_CHILD || type == LessLexer.NTH_OF_TYPE || type == LessLexer.NTH_LAST_OF_TYPE || type == LessLexer.IDENT_EXTEND;
  }

  /**
   * Basically, this is an ugly hack. The parser grammar is unable to tell the difference 
   * between "identifier (expression)" and "identifier(expression)". They differ only 
   * in whitespace which is hidden.
   * 
   * The first one represents two expressions joined by an empty operator and the second
   * one represents a function. 
   * 
   * This method takes two tokens and decides whether this is how a function should start.
   *
   * @param first
   * @param second
   * 
   * @return <code>true</code> if it is possible for a function to start with these two tokens.
   */
  public boolean onFunctionStart(TokenStream input) {
    return isFunctionStart(input, 1);
  }

  //identifier contains either:
  // * simple function name and is followed by a parenthesis (e.g. "name()")
  // * composed function name (e.g. "name:something.else ... " and is followed by a parenthesis)
  //there must be no space between function name and parenthesis with arguments
  private boolean isFunctionStart(TokenStream input, int indx) {
    Token first = input.LT(indx);
    if (first == null)
      return false;

    int ft = first.getType();
    if (!(ft == LessParser.IDENT || ft == LessParser.PERCENT))
      return false;

    Token previous = first;
    Token next = input.LT(++indx);

    while (next != null && directlyFollows(previous, next)) {
      int nt = next.getType();
      if (nt == LessParser.LPAREN)
        return true;

      if (!(nt == LessParser.IDENT || nt == LessParser.PERCENT || nt == LessParser.COLON || nt == LessParser.DOT))
        return false;

      previous = next;
      next = input.LT(++indx);
    }

    return false;
  }

  /**
   * This decides whether the parser should place an empty separator between 
   * the previousT and the firstT. 
   * 
   * The method is used for expressions parsing. It is useless in any 
   * other context.
   * 
   */
  public boolean onEmptySeparator(TokenStream input) {
    return isEmptySeparator(input.LT(-1), input.LT(1), input.LT(2));
  }

  private boolean isEmptySeparator(Token previousT, Token firstT, Token secondT) {
    //expression can not start with an empty separator
    if (previousT == null)
      return false;

    if (!(previousT instanceof CommonToken) || !(firstT instanceof CommonToken) || !(secondT instanceof CommonToken))
      return false;

    CommonToken previous = (CommonToken) previousT;
    CommonToken first = (CommonToken) firstT;
    CommonToken second = (CommonToken) secondT;

    //Separator can not follow an operator. It must follow an expression.
    if (isArithmeticOperator(previous))
      return false;

    //if it is followed by arithmetic operator, then there must be a whitespace in order to have an empty separator
    //it is because we need to distinuish between 10 +5 (list) and 15+5 (math)
    if (directlyFollows(previous, first) && isOperator(first))
      return false;

    //easy case: "10 + -23"  => plus is the operation
    if (isArithmeticOperator(first) && isArithmeticOperator(second))
      return false;

    //easy case: "10 + 23" space between "+" and "23" => plus is the operation
    if (isArithmeticOperator(first) && !isArithmeticOperator(second) && !directlyFollows(first, second))
      return false;

    return true;
  }

  private boolean isOperator(CommonToken previous) {
    return isArithmeticOperator(previous) || previous.getType() == LessLexer.COMMA;
  }

  private boolean isArithmeticOperator(CommonToken previous) {
    return previous.getType() == LessLexer.MINUS || previous.getType() == LessLexer.PLUS || previous.getType() == LessLexer.STAR || previous.getType() == LessLexer.SOLIDUS;
  }

  public boolean directlyFollows(TokenStream input) {
    return directlyFollows(input.LT(-1), input.LT(1));
  }

  public boolean notSemi(TokenStream input) {
    return notSemi(input.LT(1));
  }

  public boolean notSemi(Token token) {
    return !isSemi(token);
  }

  private boolean isSemi(Token token) {
    if (token == null || !(token instanceof CommonToken) || token.getType() != LessParser.SEMI)
      return false;

    return true;
  }

  public boolean directlyFollows(Token first, Token second) {
    if (!(first instanceof CommonToken) || !(second instanceof CommonToken))
      return false;

    CommonToken firstT = (CommonToken) first;
    CommonToken secondT = (CommonToken) second;

    return directlyFollows(firstT, secondT);
  }

  private boolean directlyFollows(CommonToken firstT, CommonToken secondT) {
    if (firstT.getStopIndex() + 1 != secondT.getStartIndex())
      return false;

    //some tokens tend to eat up following whitespaces too
    String text = firstT.getText();
    if (text == null || text.isEmpty())
      return true;

    String substring = text.substring(text.length() - 1);
    return substring.equals(substring.trim());
  }

  public boolean onEmptyCombinator(TokenStream input) {
    return isEmptyCombinator(input.LT(-1), input.LT(1));
  }

  private boolean isEmptyCombinator(Token first, Token second) {
    return !directlyFollows(first, second);
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
