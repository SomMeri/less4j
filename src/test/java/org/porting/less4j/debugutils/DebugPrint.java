package org.porting.less4j.debugutils;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
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

  public static void print(CommonTree ast) {
    print(ast, 0);
  }

  public static void print(CommonTree tree, int level) {
    // indent level
    for (int i = 0; i < level; i++)
      System.out.print("--");

    // print node description: type code followed by token text
    System.out.println(" " + tree.getType() + " " + tree.getText());

    // print all children
    if (tree.getChildren() != null)
      for (Object ie : tree.getChildren()) {
        print((CommonTree) ie, level + 1);
      }
  }

}
