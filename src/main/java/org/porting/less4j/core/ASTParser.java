package org.porting.less4j.core;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.porting.less4j.core.parser.LessLexer;
import org.porting.less4j.core.parser.LessParser;

/**
 * This class is NOT thread safe. 
 *
 */
public class ASTParser {

  private List<RecognitionException> errors = new ArrayList<RecognitionException>();
  private LessLexer lexer;
  private LessParser parser;

  public CommonTree compile(String expression) {
    try {
      initialize(expression);
      ParserRuleReturnScope ret = parser.styleSheet();
      return finalize(ret);
    } catch (RecognitionException e) {
      throw new IllegalStateException("Recognition exception is never thrown, only declared.");
    }
  }

  public CommonTree compileDeclaration(String expression) {
    try {
      initialize(expression);
      ParserRuleReturnScope ret = parser.declaration();
      return finalize(ret);
    } catch (RecognitionException e) {
      throw new IllegalStateException("Recognition exception is never thrown, only declared.");
    }
  }
  
  public CommonTree compileExpression(String expression) {
    try {
      initialize(expression);
      ParserRuleReturnScope ret = parser.expr();
      return finalize(ret);
    } catch (RecognitionException e) {
      throw new IllegalStateException("Recognition exception is never thrown, only declared.");
    }
  }

  public CommonTree compileSelector(String selector) {
    try {
      initialize(selector);
      ParserRuleReturnScope ret = parser.selector();
      return finalize(ret);
    } catch (RecognitionException e) {
      throw new IllegalStateException("Recognition exception is never thrown, only declared.");
    }
  }

  private void initialize(String expression) {
    errors = new ArrayList<RecognitionException>();
    lexer = createLexer(expression);
    parser = createParser(lexer);
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

  private CommonTree finalize(ParserRuleReturnScope ret) {
    collectErrors(lexer, parser);
    CommonTree ast = (CommonTree) ret.getTree();
    return ast;
  }

  private void collectErrors(LessLexer lexer, LessParser parser) {
    errors.addAll(lexer.getAllErrors());
    errors.addAll(parser.getAllErrors());
  }

  //add new method
  public List<RecognitionException> getAllErrors() {
    return new ArrayList<RecognitionException>(errors);
  }

}