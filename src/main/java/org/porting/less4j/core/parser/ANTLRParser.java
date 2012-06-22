package org.porting.less4j.core.parser;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.porting.less4j.core.parser.LessLexer;
import org.porting.less4j.core.parser.LessParser;

/**
 * This class is NOT thread safe. 
 *
 */
public class ANTLRParser {

  private List<RecognitionException> errors = new ArrayList<RecognitionException>();
  private LessLexer lexer;
  private LessParser parser;

  public CommonTree parse(String expression) {
    try {
      initialize(expression);
      ParserRuleReturnScope ret = parser.styleSheet();
      return finalize(ret);
    } catch (RecognitionException e) {
      throw new IllegalStateException("Recognition exception is never thrown, only declared.");
    }
  }

  public CommonTree parseDeclaration(String expression) {
    try {
      initialize(expression);
      ParserRuleReturnScope ret = parser.declaration();
      return finalize(ret);
    } catch (RecognitionException e) {
      throw new IllegalStateException("Recognition exception is never thrown, only declared.");
    }
  }
  
  public CommonTree parseExpression(String expression) {
    try {
      initialize(expression);
      ParserRuleReturnScope ret = parser.expr();
      return finalize(ret);
    } catch (RecognitionException e) {
      throw new IllegalStateException("Recognition exception is never thrown, only declared.");
    }
  }

  public CommonTree parseSelector(String selector) {
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
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    LessParser parser = new LessParser(tokens);
    //TODO: attach comments and whitespaces to tokens 
    //how to do it: get token position and attach all previous whitespaces, comments and whatever
    //parser.setTreeAdaptor(new HiddenAwareTreeAdaptor());
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

//class HiddenAwareTreeAdaptor extends CommonTreeAdaptor implements TreeAdaptor {
//
//  @Override
//  public Object dupNode(Object t) {
//    // TODO Auto-generated method stub
//    return super.dupNode(t);
//  }
//
//  @Override
//  public Object create(Token payload) {
//    int tokenIndex = payload.getTokenIndex();
//    // TODO Auto-generated method stub
//    return super.create(payload);
//  }
//
//  @Override
//  public Token createToken(int tokenType, String text) {
//    // TODO Auto-generated method stub
//    return super.createToken(tokenType, text);
//  }
//
//  @Override
//  public Token createToken(Token fromToken) {
//    // TODO Auto-generated method stub
//    return super.createToken(fromToken);
//  }
//  
//}
//
