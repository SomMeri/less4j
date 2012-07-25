package org.porting.less4j.core.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.porting.less4j.debugutils.DebugPrint;

/**
 * This class is NOT thread safe. 
 * 
 * This is statefull class.
 * 
 */
// FIXME: add handling of filter: alpha(opacity=100);
public class ANTLRParser {

  private List<RecognitionException> errors = new ArrayList<RecognitionException>();
  private LessLexer lexer;
  private LessParser parser;
  private HiddenTokensCollectorTokenSource tokenSource;

  public HiddenTokenAwareTree parse(String expression) {
    try {
      initialize(expression);
      //DebugPrint.printTokenStream(expression);
      ParserRuleReturnScope ret = parser.styleSheet();
      return finalize(ret);
    } catch (RecognitionException e) {
      throw new IllegalStateException("Recognition exception is never thrown, only declared.");
    }
  }

  public HiddenTokenAwareTree parseDeclaration(String expression) {
    try {
      initialize(expression);
      ParserRuleReturnScope ret = parser.declaration();
      return finalize(ret);
    } catch (RecognitionException e) {
      throw new IllegalStateException("Recognition exception is never thrown, only declared.");
    }
  }

  public HiddenTokenAwareTree parseExpression(String expression) {
    try {
      initialize(expression);
      ParserRuleReturnScope ret = parser.expr();
      return finalize(ret);
    } catch (RecognitionException e) {
      throw new IllegalStateException("Recognition exception is never thrown, only declared.");
    }
  }

  public HiddenTokenAwareTree parseTerm(String expression) {
    try {
      initialize(expression);
      ParserRuleReturnScope ret = parser.term();
      return finalize(ret);
    } catch (RecognitionException e) {
      throw new IllegalStateException("Recognition exception is never thrown, only declared.");
    }
  }

  public HiddenTokenAwareTree parseSelector(String selector) {
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
    //FIXME: make this consistent 
    HiddenTokensCollectorTokenSource tokenSource = new HiddenTokensCollectorTokenSource(lexer,  Arrays.asList(LessLexer.COMMENT, LessLexer.NEW_LINE));
    this.tokenSource = tokenSource;
    CommonTokenStream tokens = new CommonTokenStream(tokenSource);
    LessParser parser = new LessParser(tokens);
    parser.setTreeAdaptor(new LessTreeAdaptor());
    return parser;
  }

  private LessLexer createLexer(String expression) {
    ANTLRStringStream input = new ANTLRStringStream(expression);
    LessLexer lexer = new LessLexer(input);
    return lexer;
  }

  private HiddenTokenAwareTree finalize(ParserRuleReturnScope ret) {
    collectErrors(lexer, parser);
    HiddenTokenAwareTree ast = (HiddenTokenAwareTree) ret.getTree();
    LinkedList<Token> hiddenTokens = tokenSource.getCollectedTokens();
    CommentTreeCombiner combiner = new CommentTreeCombiner();
    combiner.associate(ast, hiddenTokens);
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

class HiddenTokensCollectorTokenSource implements TokenSource {
  
  private final TokenSource source;
  private final Set<Integer> tokenTypes = new HashSet<Integer>();
  private final LinkedList<Token> collectedTokens = new LinkedList<Token>();
  
  public HiddenTokensCollectorTokenSource(TokenSource source, Collection<Integer> tokenTypes) {
    super();
    this.source = source;
    this.tokenTypes.addAll(tokenTypes);
  }

  public LinkedList<Token> getCollectedTokens() {
    return collectedTokens;
  }

  public Token nextToken() {
    Token nextToken = source.nextToken();
    if (isPassable(nextToken)) {
      //Each token is read from the token source only once.
      //This is not documented, but it is not reasonable to build unnecessary
      //structures. Antlr3 will not have major updates and antl4 will not
      //be compatible with it.
      collectedTokens.add(nextToken);
    }
    
    return nextToken;
  }

  public boolean isPassable(Token nextToken) {
    return tokenTypes.contains(nextToken.getType());
  }

  public String getSourceName() {
    return "Collect hidden channel " + source.getSourceName();
  }
 
}

class LessTreeAdaptor extends CommonTreeAdaptor {

  @Override
  public Object create(Token payload) {
    return new HiddenTokenAwareTree(payload);
  }

}
