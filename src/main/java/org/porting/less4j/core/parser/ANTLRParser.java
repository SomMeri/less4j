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
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTreeAdaptor;
//import org.porting.less4j.debugutils.DebugPrint;
import org.porting.less4j.debugutils.DebugPrint;

/**
 * 
 * 
 */
public class ANTLRParser {

  private static final List<Integer> KEEP_HIDDEN_TOKENS = Arrays.asList(LessLexer.COMMENT, LessLexer.NEW_LINE);

  public ParseResult parseStyleSheet(String styleSheet) {
    return parse(styleSheet, InputType.STYLE_SHEET);
  }

  public ParseResult parseDeclaration(String declaration) {
    return parse(declaration, InputType.DECLARATION);
  }

  public ParseResult parseExpression(String expression) {
    return parse(expression, InputType.EXPRESSION);
  }

  public ParseResult parseTerm(String term) {
    return parse(term, InputType.TERM);
  }

  public ParseResult parseSelector(String selector) {
    return parse(selector, InputType.SELECTOR);
  }

  public ParseResult parseRuleset(String ruleset) {
    return parse(ruleset, InputType.RULESET);
  }
  
  private ParseResult parse(String input, InputType inputType) {
    try {
      DebugPrint.printTokenStream(input);
      List<RecognitionException> errors = new ArrayList<RecognitionException>();
      LessLexer lexer = createLexer(input, errors);

      CollectorTokenSource tokenSource = new CollectorTokenSource(lexer, KEEP_HIDDEN_TOKENS);
      LessParser parser = createParser(tokenSource, errors);
      ParserRuleReturnScope returnScope = inputType.parseTree(parser);
      
      HiddenTokenAwareTree ast = (HiddenTokenAwareTree) returnScope.getTree();
      merge(ast, tokenSource.getCollectedTokens());
      DebugPrint.print(ast);
      return new ParseResultImpl(ast, new ArrayList<RecognitionException>(errors));
    } catch (RecognitionException e) {
      throw new IllegalStateException("Recognition exception is never thrown, only declared.");
    }
  }

  private LessParser createParser(TokenSource tokenSource, List<RecognitionException> errors) {
    CommonTokenStream tokens = new CommonTokenStream(tokenSource);
    LessParser parser = new LessParser(tokens, errors);
    parser.setTreeAdaptor(new HiddenTokenAwareTreeAdaptor());
    return parser;
  }

  private LessLexer createLexer(String expression, List<RecognitionException> errors) {
    ANTLRStringStream input = new ANTLRStringStream(expression);
    LessLexer lexer = new LessLexer(input, errors);
    return lexer;
  }

  private HiddenTokenAwareTree merge(HiddenTokenAwareTree ast, LinkedList<Token> hiddenTokens) {
    ListToTreeCombiner combiner = new ListToTreeCombiner();
    combiner.associate(ast, hiddenTokens);
    return ast;
  }

  public interface ParseResult {
    HiddenTokenAwareTree getTree();
    List<RecognitionException> getErrors();
    boolean hasErrors();
  }

  private class ParseResultImpl implements ParseResult {
    private final HiddenTokenAwareTree tree;
    private final List<RecognitionException> errors;

    public ParseResultImpl(HiddenTokenAwareTree tree, List<RecognitionException> errors) {
      super();
      this.tree = tree;
      this.errors = errors;
    }

    public HiddenTokenAwareTree getTree() {
      return tree;
    }

    public List<RecognitionException> getErrors() {
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
    return "Collect hidden channel " + source.getSourceName();
  }

}

class HiddenTokenAwareTreeAdaptor extends CommonTreeAdaptor {

  @Override
  public Object create(Token payload) {
    return new HiddenTokenAwareTree(payload);
  }

  @Override
  public Object errorNode(TokenStream input, Token start, Token stop, RecognitionException e) {
    return new HiddenTokenAwareErrorTree(input, start, stop, e);
  }

}
