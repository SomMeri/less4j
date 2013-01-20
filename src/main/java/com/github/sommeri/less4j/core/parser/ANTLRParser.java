package com.github.sommeri.less4j.core.parser;

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
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTreeAdaptor;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.LessCompiler.Problem;
import com.github.sommeri.less4j.utils.DebugAndTestPrint;

/**
 * 
 * 
 */
public class ANTLRParser {

  private final boolean isDebug = false;
  private static final List<Integer> KEEP_HIDDEN_TOKENS = Arrays.asList(LessLexer.COMMENT, LessLexer.NEW_LINE);

  public ParseResult parseStyleSheet(String styleSheet, LessSource source) {
    return parse(styleSheet, source, InputType.STYLE_SHEET);
  }

  public ParseResult parseDeclaration(String declaration, LessSource source) {
    return parse(declaration, source, InputType.DECLARATION);
  }

  public ParseResult parseExpression(String expression, LessSource source) {
    return parse(expression, source, InputType.EXPRESSION);
  }

  public ParseResult parseTerm(String term, LessSource source) {
    return parse(term, source, InputType.TERM);
  }

  public ParseResult parseSelector(String selector, LessSource source) {
    return parse(selector, source, InputType.SELECTOR);
  }

  public ParseResult parseRuleset(String ruleset, LessSource source) {
    return parse(ruleset, source, InputType.RULESET);
  }
  
  private ParseResult parse(String input, LessSource source, InputType inputType) {
    try {
      if (isDebug)
        DebugAndTestPrint.printTokenStream(input);
      List<Problem> errors = new ArrayList<Problem>();
      LessLexer lexer = createLexer(input, source, errors);

      CollectorTokenSource tokenSource = new CollectorTokenSource(lexer, KEEP_HIDDEN_TOKENS);
      LessParser parser = createParser(tokenSource, source, errors);
      ParserRuleReturnScope returnScope = inputType.parseTree(parser);
      
      HiddenTokenAwareTree ast = (HiddenTokenAwareTree) returnScope.getTree();
      merge(ast, tokenSource.getCollectedTokens());
      if (isDebug)
        DebugAndTestPrint.print(ast);
      return new ParseResultImpl(ast, new ArrayList<Problem>(errors));
    } catch (RecognitionException e) {
      throw new IllegalStateException("Recognition exception is never thrown, only declared.");
    }
  }

  private LessParser createParser(TokenSource tokenSource, LessSource source, List<Problem> errors) {
    CommonTokenStream tokens = new CommonTokenStream(tokenSource);
    LessParser parser = new LessParser(tokens, errors);
    parser.setTreeAdaptor(new HiddenTokenAwareTreeAdaptor(source));
    return parser;
  }

  private LessLexer createLexer(String expression, LessSource source, List<Problem> errors) {
    ANTLRStringStream input = new ANTLRStringStream(expression);
    LessLexer lexer = new LessLexer(source, input, errors);
    return lexer;
  }

  private HiddenTokenAwareTree merge(HiddenTokenAwareTree ast, LinkedList<Token> hiddenTokens) {
    ListToTreeCombiner combiner = new ListToTreeCombiner();
    combiner.associate(ast, hiddenTokens);
    return ast;
  }

  public interface ParseResult {
    HiddenTokenAwareTree getTree();
    List<Problem> getErrors();
    boolean hasErrors();
  }

  private class ParseResultImpl implements ParseResult {
    private final HiddenTokenAwareTree tree;
    private final List<Problem> errors;

    public ParseResultImpl(HiddenTokenAwareTree tree, List<Problem> errors) {
      super();
      this.tree = tree;
      this.errors = errors;
    }

    public HiddenTokenAwareTree getTree() {
      return tree;
    }

    public List<Problem> getErrors() {
      return errors;
    }

    @Override
    public boolean hasErrors() {
      return !(getErrors()==null || getErrors().isEmpty());
    }

  }

  private enum InputType {
    SELECTOR {
      @Override
      public ParserRuleReturnScope parseTree(LessParser parser) throws RecognitionException {
        return parser.selector();
      }
    },
    TERM {
      @Override
      public ParserRuleReturnScope parseTree(LessParser parser) throws RecognitionException {
        return parser.term();
      }
    },
    EXPRESSION {
      @Override
      public ParserRuleReturnScope parseTree(LessParser parser) throws RecognitionException {
        return parser.expr();
      }
    },
    DECLARATION {
      @Override
      public ParserRuleReturnScope parseTree(LessParser parser) throws RecognitionException {
        return parser.declaration();
      }
    },
    STYLE_SHEET {
      @Override
      public ParserRuleReturnScope parseTree(LessParser parser) throws RecognitionException {
        return parser.styleSheet();
      }
    }, RULESET {
      @Override
      public ParserRuleReturnScope parseTree(LessParser parser) throws RecognitionException {
        return parser.ruleSet();
      }
    };

    public abstract ParserRuleReturnScope parseTree(LessParser parser) throws RecognitionException;

  }

}

class CollectorTokenSource implements TokenSource {

  private final TokenSource source;
  private final Set<Integer> collectTokenTypes = new HashSet<Integer>();
  private final LinkedList<Token> collectedTokens = new LinkedList<Token>();

  public CollectorTokenSource(TokenSource source, Collection<Integer> collectTokenTypes) {
    super();
    this.source = source;
    this.collectTokenTypes.addAll(collectTokenTypes);
  }

  /**
   * Returns next token from the wrapped token source. Stores it in a list if
   * necessary.
   * 
   */
  @Override
  public Token nextToken() {
    Token nextToken = source.nextToken();
    if (shouldCollect(nextToken)) {
      collectedTokens.add(nextToken);
    }

    return nextToken;
  }

  /**
   * Decide whether collect the token or not.
   */
  protected boolean shouldCollect(Token nextToken) {
    // filter the token by its type
    return collectTokenTypes.contains(nextToken.getType());
  }

  public LinkedList<Token> getCollectedTokens() {
    return collectedTokens;
  }

  @Override
  public String getSourceName() {
    return source.getSourceName();
  }

}

class HiddenTokenAwareTreeAdaptor extends CommonTreeAdaptor {

  private final LessSource source;

  public HiddenTokenAwareTreeAdaptor(LessSource source) {
    super();
    this.source = source;
  }

  @Override
  public Object create(Token payload) {
    return new HiddenTokenAwareTree(payload, source);
  }

  @Override
  public Object errorNode(TokenStream input, Token start, Token stop, RecognitionException e) {
    return new HiddenTokenAwareErrorTree(input, start, stop, e, source);
  }

  public LessSource getSource() {
    return source;
  }

}
