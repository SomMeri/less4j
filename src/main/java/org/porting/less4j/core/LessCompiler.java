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

public class LessCompiler {

  private List<RecognitionException> errors = new ArrayList<RecognitionException>();

  public CommonTree compile(String expression) {
    try {
      errors = new ArrayList<RecognitionException>();
      // lexer splits input into tokens
      ANTLRStringStream input = new ANTLRStringStream(expression);
      LessLexer lexer = new LessLexer(input);
      TokenStream tokens = new CommonTokenStream(lexer);

      // parser generates abstract syntax tree
      LessParser parser = new LessParser(tokens);
      LessParser.styleSheet_return ret = parser.styleSheet();

      errors.addAll(lexer.getAllErrors());
      errors.addAll(parser.getAllErrors());
      
      // acquire parse result
      CommonTree ast = (CommonTree) ret.getTree();
      printTree(ast);
      return ast;
    } catch (RecognitionException e) {
      throw new IllegalStateException("Recognition exception is never thrown, only declared.");
    }
  }

  public CommonTree compileExpression(String expression) {
    try {
      // lexer splits input into tokens
      ANTLRStringStream input = new ANTLRStringStream(expression);
      TokenStream tokens = new CommonTokenStream(new LessLexer(input));

      // parser generates abstract syntax tree
      LessParser parser = new LessParser(tokens);
      LessParser.expr_return ret = parser.expr();

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
}