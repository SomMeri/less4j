package org.porting.less4j.core;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.porting.less4j.core.parser.LessLexer;
import org.porting.less4j.core.parser.LessParser;
import org.porting.less4j.core.parser.LessParser.declaration_return;
import org.porting.less4j.core.parser.LessParser.expr_return;

public class ASTParser {

  private List<RecognitionException> errors = new ArrayList<RecognitionException>();

  public CommonTree compile(String expression) {
    try {
      cleanErrors();

      LessLexer lexer = createLexer(expression);
      LessParser parser = createParser(lexer);
      LessParser.styleSheet_return ret = parser.styleSheet();

      collectErrors(lexer, parser);
      
      // acquire parse result
      CommonTree ast = (CommonTree) ret.getTree();
      
      printTree(ast);
      return ast;
    } catch (RecognitionException e) {
      throw new IllegalStateException("Recognition exception is never thrown, only declared.");
    }
  }

  private void collectErrors(LessLexer lexer, LessParser parser) {
    errors.addAll(lexer.getAllErrors());
    errors.addAll(parser.getAllErrors());
  }

  private void cleanErrors() {
    errors = new ArrayList<RecognitionException>();
  }

  private LessParser createParser(LessLexer lexer) {
    TokenStream tokens = new CommonTokenStream(lexer);
    LessParser parser = new LessParser(tokens);
    return parser;
  }

  private LessLexer createLexer(String expression) {
    ANTLRStringStream input = new ANTLRStringStream(expression);
    LessLexer lexer = new LessLexer(input);
    return lexer;
  }

  public CommonTree compileDeclaration(String expression) {
    try {
      cleanErrors();
      LessLexer lexer = createLexer(expression);
      LessParser parser = createParser(lexer);
      declaration_return ret = parser.declaration();

      collectErrors(lexer, parser);

      // acquire parse result
      CommonTree ast = (CommonTree) ret.getTree();
      printTree(ast);
      return ast;
    } catch (RecognitionException e) {
      throw new IllegalStateException("Recognition exception is never thrown, only declared.");
    }
  }
  
  private void printTree(CommonTree ast) {
    print(ast, 0);
  }

  private void print(CommonTree tree, int level) {
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

  //add new method
  public List<RecognitionException> getAllErrors() {
    return new ArrayList<RecognitionException>(errors);
  }

  public CommonTree compileExpression(String expression) {
    try {
      cleanErrors();
      LessLexer lexer = createLexer(expression);
      LessParser parser = createParser(lexer);
      expr_return ret = parser.expr();

      collectErrors(lexer, parser);

      // acquire parse result
      CommonTree ast = (CommonTree) ret.getTree();
      printTree(ast);
      return ast;
    } catch (RecognitionException e) {
      throw new IllegalStateException("Recognition exception is never thrown, only declared.");
    }
  }
}