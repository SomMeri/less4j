package com.github.sommeri.less4j.core.parser;

import java.util.HashSet;
import java.util.Set;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;

import com.github.sommeri.less4j.core.parser.LessLexer;
import com.github.sommeri.less4j.core.parser.LessParser;

public class ParsersSemanticPredicates {

  private static Set<String> NTH_PSEUDOCLASSES = new HashSet<String>();
  static {
    NTH_PSEUDOCLASSES.add("nth-child");
    NTH_PSEUDOCLASSES.add("nth-last-child");
    NTH_PSEUDOCLASSES.add("nth-of-type");
    NTH_PSEUDOCLASSES.add("nth-last-of-type");
  }

  public boolean isNthPseudoClass(Token a) {
    if (a == null)
      return false;
    String text = a.getText();
    if (text == null)
      return false;
    return NTH_PSEUDOCLASSES.contains(text.toLowerCase());
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
  public boolean isFunctionStart(Token first, Token second) {
    if (first==null || second==null)
      return false;
    
    //identifier contains function name and is followed by a parenthesis
    if (first.getType()!=LessParser.IDENT || second.getType()!=LessParser.LPAREN)
      return false;
    
    //there must be no space between function name and parenthesis with arguments
    return directlyFollows(first, second);
  }

  /**
   * This decides whether the parser should place an empty separator between 
   * the previousT and the firstT. 
   * 
   * The method is used for expressions parsing. It is useless in any 
   * other context.
   * 
   */
  public boolean isEmptySeparator(Token previousT, Token firstT, Token secondT) {
    //expression can not start with an empty separator
    if (previousT==null)
      return false;
    
    if (!(previousT instanceof CommonToken) || !(firstT instanceof CommonToken) || !(secondT instanceof CommonToken))
      return false;
    
    CommonToken previous = (CommonToken) previousT;
    CommonToken first = (CommonToken) firstT;
    CommonToken second = (CommonToken) secondT;

    //Separator can not follow an operator. It must follow an expression.
    if (isArithmeticOperator(previous)) 
      return false;
    
    //there must be a whitespace in order to have an empty separator
    if (directlyFollows(previous, first))
      return false;
    
    //easy case: "10 + -23"  => plus is the operation
    if (isArithmeticOperator(first) && isArithmeticOperator(second))
      return false;

    //easy case: "10 + 23" space between "+" and "23" => plus is the operation
    if (isArithmeticOperator(first) && !isArithmeticOperator(second) && !directlyFollows(first, second))
      return false;

    return true;
  }

  private boolean isArithmeticOperator(CommonToken previous) {
    return previous.getType()==LessLexer.MINUS || previous.getType()==LessLexer.PLUS || previous.getType()==LessLexer.STAR  || previous.getType()==LessLexer.SOLIDUS;
  }

  public boolean directlyFollows(Token first, Token second) {
    if (!(first instanceof CommonToken) || !(second instanceof CommonToken))
      return false;
    
    CommonToken firstT = (CommonToken) first;
    CommonToken secondT = (CommonToken) second;
    
    return directlyFollows(firstT, secondT);
  }

  public boolean directlyFollows(CommonToken firstT, CommonToken secondT) {
    if (firstT.getStopIndex()+1!= secondT.getStartIndex())
      return false;

    //some tokens tend to eat up following whitespaces too
    String text = firstT.getText();
    if (text==null || text.isEmpty())
      return true;
    
    String substring = text.substring(text.length()-1);
    return substring.equals(substring.trim());
  }

  public boolean isWhenKeyword(Token token) {
    if (token==null || !(token instanceof CommonToken) || token.getType()!=LessParser.IDENT)
      return false;
    
    String word = token.getText().trim().toLowerCase();
    return "when".equals(word);
  }

}
