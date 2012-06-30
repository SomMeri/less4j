package org.porting.less4j.debugutils;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.porting.less4j.core.parser.LessLexer;

public class DebugPrint {

  private static boolean full = true;

  public static void printTokenStream(String expression) {
    LessLexer createLexer = createLexer(expression);
    Token token = createLexer.nextToken();
    int count = 0;
    while (token.getType() != LessLexer.EOF) {
      System.out.println(toString(count++, token));
      token = createLexer.nextToken();
    }
  }

  private static LessLexer createLexer(String expression) {
    ANTLRStringStream input = new ANTLRStringStream(expression);
    LessLexer lexer = new LessLexer(input);
    return lexer;
  }

  private static String toString(int count, Token token) {
    return "(" + count + ") " + token.getType() + " " + token.getText();
  }

  public static void print(CommonTree ast) {
    print(ast, 0);
  }

  private static void print(CommonTree tree, int level) {
    indent(level);
    printSingleNode(tree);

    // print all children
    if (tree.getChildren() != null)
      for (Object ie : tree.getChildren()) {
        print((CommonTree) ie, level + 1);
      }
  }

  public static void indent(int level) {
    for (int i = 0; i < level; i++)
      System.out.print("--");
  }

  public static void printSingleNode(CommonTree tree) {
    String base = " " + tree.getType() + " " + tree.getText();
    String optionalsuffix = " " + tree.getTokenStartIndex() + " " + tree.getTokenStopIndex() + " " + (tree.getToken() == null ? "" : tree.getToken().getTokenIndex());
    if (full)
      System.out.println(base + optionalsuffix);
    else
      System.out.println(base);
  }

}
