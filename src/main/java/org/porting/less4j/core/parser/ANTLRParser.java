package org.porting.less4j.core.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.porting.less4j.debugutils.DebugPrint;

/**
 * This class is NOT thread safe.
 * 
 */
// FIXME: try to handle missing semicolon in rulecatch of declaration_complete <- I can not do that because
// the catch clause does not handle that. 
// FIXME: add handling of filter: alpha(opacity=100);
public class ANTLRParser {

  private List<RecognitionException> errors = new ArrayList<RecognitionException>();
  private LessLexer lexer;
  private LessParser parser;

  public CommonTree parse(String expression) {
    try {
      initialize(expression);
      DebugPrint.printTokenStream(expression);
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
    SelectiveGrammarCallback callback = new SelectiveGrammarCallback();
    CommentsCollectingTokenSource tokenSource = new CommentsCollectingTokenSource(lexer,  callback);
    CommonTokenStream tokens = new CommonTokenStream(tokenSource);
    LessParser parser = new LessParser(callback, tokens);
    // TODO: attach comments and whitespaces to tokens
    // how to do it: get token position and attach all previous whitespaces,
    // comments and whatever
    // parser.setTreeAdaptor(new HiddenAwareTreeAdaptor());
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
    DebugPrint.print(ast);
    return ast;
  }

  private void collectErrors(LessLexer lexer, LessParser parser) {
    errors.addAll(lexer.getAllErrors());
    errors.addAll(parser.getAllErrors());
  }

  // add new method
  public List<RecognitionException> getAllErrors() {
    return new ArrayList<RecognitionException>(errors);
  }

}

class CommentsCollectingTokenSource implements TokenSource {
  
  private final TokenSource source;
  //FIXME NOW: move to interface and make nicer
  private final Set<Integer> channels = new HashSet<Integer>();
  private final Set<Integer> tokenTypes = new HashSet<Integer>();
  private final ILessGrammarCallback callback;

  public CommentsCollectingTokenSource(TokenSource source, ILessGrammarCallback callback) {
    super();
    this.source = source;
    this.callback = callback;
    channels.add(2);
    tokenTypes.add(LessLexer.COMMENT);
  }

  public Token nextToken() {
    Token nextToken = source.nextToken();
    if (isPassable(nextToken)) {
      callback.skippingOffChannelToken(nextToken);
    }
    
    return nextToken;
  }

  public boolean isPassable(Token nextToken) {
    return channels.contains(nextToken.getChannel()) && tokenTypes.contains(nextToken.getType());
  }

  public String getSourceName() {
    return "Comments Collecting " + source.getSourceName();
  }
 
}

