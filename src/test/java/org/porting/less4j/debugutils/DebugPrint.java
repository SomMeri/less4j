package org.porting.less4j.debugutils;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.Token;
import org.porting.less4j.core.parser.LessLexer;

public class DebugPrint {
  
  public static void printTokenStream(String expression){
    LessLexer createLexer = createLexer(expression);
    Token token = createLexer.nextToken();
    while (token.getType()!=LessLexer.EOF){ 
      System.out.println(toString(token));
      token = createLexer.nextToken();
    }
  }
  
  private static LessLexer createLexer(String expression) {
    ANTLRStringStream input = new ANTLRStringStream(expression);
    LessLexer lexer = new LessLexer(input);
    return lexer;
  }

  private static String toString(Token token) {
    return " " + token.getType() + " " + token.getText();
  }

}
