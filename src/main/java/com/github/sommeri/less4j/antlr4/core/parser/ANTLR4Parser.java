package com.github.sommeri.less4j.antlr4.core.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.misc.Utils;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.Tree;
import org.antlr.v4.runtime.tree.Trees;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.parser.LessG4Lexer;
import com.github.sommeri.less4j.core.parser.LessG4Parser;
import com.github.sommeri.less4j.utils.debugonly.DebugAndTestPrint;

public class ANTLR4Parser {

  private static final boolean isDebug = true;

  public ParserResult parseStyleSheet(String content, LessSource source) {
    if (isDebug)
      DebugAndTestPrint.printTokenStream(content);

    ANTLRInputStream input = new ANTLRInputStream(content);
    // create a lexer that feeds off of input CharStream
    LessG4Lexer lexer = new LessG4Lexer(input);
    lexer.setTokenFactory(new Antlr4_TokenFactory(source));
    // create a buffer of tokens pulled from the lexer
    EnhancedTokenStream tokens = new EnhancedTokenStream(lexer);
    // create a parser that feeds off the tokens buffer
    //FIXME: (antlr4) wtf is this about? related: http://www.antlr.org/papers/allstar-techreport.pdf
    LessG4Parser parser = new LessG4Parser(tokens);
    // two stage parsing is necessary due to css3-modsel-170c
    
    parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
    ParseTree tree = null;
    try {
       tree = parser.styleSheet(); // begin parsing at styleSheet rule
    } catch (Exception ex) {
      tokens.reset(); // rewind input stream
      parser.reset();
      parser.getInterpreter().setPredictionMode(PredictionMode.LL);
      tree = parser.styleSheet();  // STAGE 2
    }
    
    FormattedTree formattedTree = new FormattedTree(null, parser);

    LinkedList<CommonToken> hiddenTokens = toCommonTokens(tokens.filterForTypes(LessG4Lexer.COMMENT, LessG4Lexer.NEW_LINE));
        
    Antlr4_ListToTreeCombiner combiner = merge(tree, hiddenTokens);
    TreeComments commentsMap = combiner.getAssociatedMap();
    Set<Entry<Tree, NodeCommentsHolder>> all = commentsMap.all();
    for (Entry<Tree, NodeCommentsHolder> entry : all) {
      NodeCommentsHolder holder = entry.getValue();
      if (!holder.getPreceding().isEmpty() || !holder.getFollowing().isEmpty() || !holder.getOrphans().isEmpty()) {
        //System.out.println(" **** " + formattedTree.getTextAndPosition(entry.getKey()));
      }
    }
    formattedTree.addToTreatAsTerminal("ws", "combinator_ws", "combinator");
    System.out.println(" --> Parse Tree <-- \n" + formattedTree.toStringTree(tree));
    return new ParserResult(tree, commentsMap);
  }

  private LinkedList<CommonToken> toCommonTokens(List<Token> from) {
    LinkedList<CommonToken> to = new LinkedList<CommonToken>();
    if (from!=null) for (Token token : from) {
      to.add((CommonToken)token);
    }
    return to;
  }

  private Antlr4_ListToTreeCombiner merge(ParseTree ast, LinkedList<CommonToken> hiddenTokens) {
    Antlr4_ListToTreeCombiner combiner = new Antlr4_ListToTreeCombiner();
    combiner.associate(ast, hiddenTokens);
    return combiner;
  }

  public class ParserResult {

    private final ParseTree tree;
    private final TreeComments comments;

    public ParserResult(ParseTree tree, TreeComments comments) {
      this.tree = tree;
      this.comments = comments;
    }

    public ParseTree getTree() {
      return tree;
    }

    public TreeComments getComments() {
      return comments;
    }
    
  }
}

class Antlr4_TokenFactory implements TokenFactory<Antlr4_HiddenTokenAwareTree> {
  private LessSource lessSource;

  public Antlr4_TokenFactory(LessSource lessSource) {
    this.lessSource = lessSource;
  }

  @Override
  public Antlr4_HiddenTokenAwareTree create(int type, String text) {
    return new Antlr4_HiddenTokenAwareTree(type, text);
  }

  @Override
  public Antlr4_HiddenTokenAwareTree create(Pair<TokenSource, CharStream> source, int type, String text, int channel, int start, int stop, int line, int charPositionInLine) {
    Antlr4_HiddenTokenAwareTree t = new Antlr4_HiddenTokenAwareTree(source, type, channel, start, stop);
    t.setLine(line);
    t.setCharPositionInLine(charPositionInLine);
    t.setSource(lessSource);
    return t;
  }
}

class FormattedTree {

  //FIXME: (antlr4) create antlr4 swiss knife lib
  public final String INDENT_STR = "  ";
  private Set<String> treatAsTerminal = new HashSet<String>();
  private TreeComments combiner;
  private List<String> ruleNamesList;
  
  //FIXME: (antlr4) accept reasonable interface Antlr4_ListToTreeCombiner does not belong here 
  public FormattedTree(TreeComments combiner, Parser parser) {
    this.combiner = combiner;
    String[] ruleNames = parser != null ? parser.getRuleNames() : null;
    this.ruleNamesList = ruleNames != null ? Arrays.asList(ruleNames) : null;
  }

  public void addToTreatAsTerminal(String... rules) {
    for (String rule : rules) {
      treatAsTerminal.add(rule);
    }
  }

  /** Print out a whole tree in LISP form. {@link #getNodeText} is used on the
   *  node payloads to get the text for the nodes.  Detect
   *  parse trees and extract data appropriately.
   */
  public String toStringTree(Tree t) {
    return toStringTree(t, ruleNamesList);
  }

  /** Print out a whole tree in LISP form. {@link #getNodeText} is used on the
   *  node payloads to get the text for the nodes.  Detect
   *  parse trees and extract data appropriately.
   */
  public String toStringTree(Tree t, List<String> ruleNames) {
    return toStringTree(t, ruleNames, 0);
  }

  public String toStringTree(Tree t, List<String> ruleNames, int indent) {
    StringBuilder buf = new StringBuilder();
    String thisNodeText = getNodeText(t);
    if (t.getChildCount() == 0 || treatAsTerminal(thisNodeText)) {
      addIndent(buf, indent);
      buf.append("\"").append(thisNodeText).append("\"");
      return buf.toString();
    }
    
    addIndent(buf, indent).append(thisNodeText).append("(");

    //buf.append(combiner.get(t).getPreceding().size());
    
    if (t.getChildCount() == 1 && t.getChild(0).getChildCount()==0) {
      String stringTree = toStringTree(t.getChild(0), ruleNames, 0);
      buf.append(stringTree).append(")");
      return buf.toString();
    }
    buf.append("\n");
    
    for (int i = 0; i < t.getChildCount(); i++) {
      String childText = toStringTree(t.getChild(i), ruleNames, indent + 1);
      String[] childLines = childText.split("\n");
      for (String line : childLines) {
        addIndent(buf, indent);
        buf.append(line);
        buf.append("\n");
      }
    }
    addIndent(buf, indent);
    buf.append(")\n");

    return buf.toString();
  }

  public String getNodeText(Tree t) {
    return Utils.escapeWhitespace(Trees.getNodeText(t, ruleNamesList), false);
  }

  public String getTextAndPosition(Tree t) {
    String nodeText = getNodeText(t);
    if (t instanceof TerminalNode) {
      TerminalNode terminal = (TerminalNode) t;
      Token token = terminal.getSymbol();
      nodeText+="(" + token.getLine() + ":" + token.getCharPositionInLine() + ")";
    } else if (t instanceof ParserRuleContext) {
      ParserRuleContext rule = (ParserRuleContext) t;
      nodeText+="(" + rule.start.getLine() + ":" + rule.start.getCharPositionInLine() + ")";
    }
    
    return nodeText;
  }

  private boolean treatAsTerminal(String ruleText) {
    return treatAsTerminal.contains(ruleText);
  }

  private StringBuilder addIndent(StringBuilder buf, int indent) {
    if (indent>0)
      buf.append(INDENT_STR);
    return buf;
  }
}

class EnhancedTokenStream extends CommonTokenStream {

  public EnhancedTokenStream(TokenSource tokenSource, int channel) {
    super(tokenSource, channel);
  }

  public EnhancedTokenStream(TokenSource tokenSource) {
    super(tokenSource);
  }
  
  public List<Token> filterForTypes(int... types) {
    return filterForTypesRanged(0, tokens.size(), types);
  }
  
  public List<Token> filterForTypesRanged(int from, int to, int... types) {
    List<Token> result = new ArrayList<Token>();
    for (int i=from; i<to; i++) {
      Token t = tokens.get(i);
      int type = t.getType();
      if (isIn(type, types)) {
        result.add(t);
      }
    }
    return result;
  }

  private boolean isIn(int lookFor, int[] inside) {
    for (int value : inside) {
      if (value==lookFor)
        return true;
    }
    return false;
  }


}