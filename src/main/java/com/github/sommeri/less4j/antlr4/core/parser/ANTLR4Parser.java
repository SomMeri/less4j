package com.github.sommeri.less4j.antlr4.core.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.ArrayPredictionContext;
import org.antlr.v4.runtime.atn.PredictionContext;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.atn.PublicPredictionContextCache;
import org.antlr.v4.runtime.atn.SingletonPredictionContext;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.misc.Utils;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.Tree;
import org.antlr.v4.runtime.tree.Trees;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.parser.LessG4Lexer;
import com.github.sommeri.less4j.core.parser.LessG4Parser;
import com.github.sommeri.less4j.utils.PerformanceUtil;
import com.github.sommeri.less4j.utils.debugonly.DebugAndTestPrint;

public class ANTLR4Parser {

  private static final String WARMED_UP_CAHE = "predictionContextCache.json";
  private static final boolean isDebug = false;
  private final PerformanceUtil performanceUtil = new PerformanceUtil();

  public ParserResult parseStyleSheet(String content, LessSource source) {
//    if (isDebug)
//      DebugAndTestPrint.printTokenStream(content);

    ANTLRInputStream input = new ANTLRInputStream(content);
    // create a lexer that feeds off of input CharStream
    LessG4Lexer lexer = new LessG4Lexer(input);
    lexer.setTokenFactory(new Antlr4_TokenFactory(source));
    // create a buffer of tokens pulled from the lexer
    EnhancedTokenStream tokens = new EnhancedTokenStream(lexer);
    // create a parser that feeds off the tokens buffer
    //FIXME: (antlr4) wtf is this about? related: http://www.antlr.org/papers/allstar-techreport.pdf
    PublicLessG4Parser parser = new PublicLessG4Parser(tokens);

    Map<PredictionContext, PredictionContext> chache = parser.getMap();
    //System.err.println("before: " + chache.size());
    if (chache.size() == 0) {
      performanceUtil.start("cache_warmup");
      String cachedJson = readFile(WARMED_UP_CAHE);
      Map<Integer, PredictionContext> parseJsonToPredictionContext = parseJsonToPredictionContext(cachedJson);
      Collection<PredictionContext> values = parseJsonToPredictionContext.values();
      for (PredictionContext predictionContext : values) {
        PredictionContextCache chache2 = parser.getChache();
        chache2.add(predictionContext);
      }
      String howLong = performanceUtil.measureAsString("cache_warmup");
//      System.err.println("Load time: " + howLong);
//      System.err.println("after load: " + chache.size());
    }

    // two stage parsing is necessary due to css3-modsel-170c
    parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
//    System.err.println("middle: " + chache.size());
    ParseTree tree = null;
    try {
      tree = parser.styleSheet(); // begin parsing at styleSheet rule
    } catch (Exception ex) {
      tokens.reset(); // rewind input stream
      parser.reset();
      parser.getInterpreter().setPredictionMode(PredictionMode.LL);
      tree = parser.styleSheet(); // STAGE 2
    }

//    DFA[] array = parser.getArray();
//    System.err.println("after: " + array.length);
//    for (DFA dfa : array) {
//      System.err.println(" -- " + dfa.states.size());
//    }
    
    //    Set<String> classes = new HashSet<String>();
    //    Set<Integer> ids = new HashSet<Integer>();
    //    for (Entry<PredictionContext, PredictionContext> entry : chache.entrySet()) {
    //      classes.add(entry.getKey().getClass().getSimpleName());
    //      classes.add(entry.getValue().getClass().getSimpleName());
    //      if (ids.contains(entry.getValue().id))
    //        System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    //
    //      ids.add(entry.getValue().id);
    //    }
    //    for (String string : classes) {
    //      System.err.println(" * " + string);
    //    }

    //    persistCache(chache);

    FormattedTree formattedTree = new FormattedTree(null, parser);

    LinkedList<CommonToken> hiddenTokens = toCommonTokens(tokens.filterForTypes(LessG4Lexer.COMMENT, LessG4Lexer.NEW_LINE));

    TreeComments commentsMap = merge(tree, hiddenTokens);
    Set<Entry<Tree, NodeCommentsHolder>> all = commentsMap.all();
    for (Entry<Tree, NodeCommentsHolder> entry : all) {
      NodeCommentsHolder holder = entry.getValue();
      if (!holder.getPreceding().isEmpty() || !holder.getFollowing().isEmpty() || !holder.getOrphans().isEmpty()) {
        //System.out.println(" **** " + formattedTree.getTextAndPosition(entry.getKey()));
      }
    }
    formattedTree.addToTreatAsTerminal("ws", "combinator_ws", "combinator");
    //System.out.println(" --> Parse Tree <-- \n" + formattedTree.toStringTree(tree));
    return new ParserResult(tree, commentsMap);
  }

  private void persistCache(Map<PredictionContext, PredictionContext> chache) {
    StringBuffer buffer = new StringBuffer();
    try {
      String jsonString = cacheToJson(chache, buffer);
      printStringToFile(WARMED_UP_CAHE, jsonString);
      System.out.println(jsonString);

      //      System.out.println(buffer);

      Map<Integer, PredictionContext> oldIdToNewObject = parseJsonToPredictionContext(jsonString);

      System.out.println(oldIdToNewObject);

    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private Map<Integer, PredictionContext> parseJsonToPredictionContext(String jsonString) {
    JSONArray jsonarray;
    try {
      jsonarray = new JSONArray(jsonString);
      Map<Integer, PredictionContext> oldIdToNewObject = new HashMap<Integer, PredictionContext>();
      oldIdToNewObject.put(Integer.valueOf(0), PredictionContext.EMPTY);

      for (int i = 0; i < jsonarray.length(); i++) {
        JSONObject object = jsonarray.getJSONObject(i);
        int oldId = object.getInt("id");
        if (object.has("returnState")) {
          int returnState = object.getInt("returnState");
          int parentid = object.getInt("parentid");
          PredictionContext parent = oldIdToNewObject.get(parentid);
          SingletonPredictionContext singleton = SingletonPredictionContext.create(parent, returnState);
          oldIdToNewObject.put(oldId, singleton);
        } else {
          JSONArray parentidsJson = object.getJSONArray("parentids");
          PredictionContext[] parents = new PredictionContext[parentidsJson.length()];
          for (int j = 0; j < parents.length; j++) {
            int parentId = parentidsJson.getInt(j);
            parents[j] = oldIdToNewObject.get(parentId);
          }

          JSONArray returnStatesJson = object.getJSONArray("returnStates");
          int[] returnStates = new int[returnStatesJson.length()];
          for (int j = 0; j < returnStates.length; j++) {
            returnStates[j] = returnStatesJson.getInt(j);
          }

          ArrayPredictionContext array = new ArrayPredictionContext(parents, returnStates);
          oldIdToNewObject.put(oldId, array);
        }

      }
      return oldIdToNewObject;
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      throw new RuntimeException(e);
    }
  }

  private String cacheToJson(Map<PredictionContext, PredictionContext> chache, StringBuffer buffer) throws JSONException {
    JSONArray toJson = new JSONArray();
    List<PredictionContext> values = new ArrayList<PredictionContext>(chache.values());
    Collections.sort(values, new Comparator<PredictionContext>() {

      @Override
      public int compare(PredictionContext arg0, PredictionContext arg1) {
        return arg0.id - arg1.id;
      }

    });

    for (PredictionContext value : values) {
      JSONObject object = new JSONObject();
      object.put("id", value.id);
      object.put("cachedHashCode", value.cachedHashCode);

      buffer.append(value.id).append(" ").append(value.cachedHashCode).append(" ");
      if (value instanceof SingletonPredictionContext) {
        SingletonPredictionContext singleton = (SingletonPredictionContext) value;
        object.put("returnState", singleton.returnState);
        object.put("parentid", singleton.parent.id);
        buffer.append(singleton.returnState).append(" ");

        //        if (value.id <= singleton.parent.id)
        //          throw new IllegalStateException();

        buffer.append("parent: " + singleton.parent.id);
      } else if (value instanceof ArrayPredictionContext) {
        ArrayPredictionContext array = (ArrayPredictionContext) value;
        buffer.append(array.returnStates.length);

        JSONArray returnStates = new JSONArray();
        for (int i = 0; i < array.returnStates.length; i++) {
          returnStates.put(array.returnStates[i]);
        }
        JSONArray parentids = new JSONArray();
        for (int i = 0; i < array.parents.length; i++) {
          parentids.put(array.parents[i].id);
          if (value.id <= array.parents[i].id)
            throw new IllegalStateException();
        }
        object.put("returnStates", returnStates);
        object.put("parentids", parentids);
      }
      buffer.append("\n");
      toJson.put(object);
    }

    System.out.println(buffer);
    String jsonString = toJson.toString();
    return jsonString;
  }

  private LinkedList<CommonToken> toCommonTokens(List<Token> from) {
    LinkedList<CommonToken> to = new LinkedList<CommonToken>();
    if (from != null)
      for (Token token : from) {
        to.add((CommonToken) token);
      }
    return to;
  }

  private TreeComments merge(ParseTree ast, LinkedList<CommonToken> hiddenTokens) {
    Antlr4_ListToTreeCombiner combiner = new Antlr4_ListToTreeCombiner();
    TreeComments result = combiner.associate(ast, hiddenTokens);
    return result;
  }

  protected String readFile(String filename) {
    return readFile(new File(filename));
  }

  protected String readFile(File file) {
    try {
      return IOUtils.toString(new InputStreamReader(new FileInputStream(file), "utf-8"));
    } catch (Throwable ex) {
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }

  private void printStringToFile(String filename, String string) {
    try {
      File file = new File(filename);
      //file.getParentFile().mkdirs();
      file.createNewFile();
      FileWriter output = new FileWriter(file);
      IOUtils.write(string, output);
      output.close();
    } catch (IOException e) {
      throw new IllegalStateException();
    }
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

    if (t.getChildCount() == 1 && t.getChild(0).getChildCount() == 0) {
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
      nodeText += "(" + token.getLine() + ":" + token.getCharPositionInLine() + ")";
    } else if (t instanceof ParserRuleContext) {
      ParserRuleContext rule = (ParserRuleContext) t;
      nodeText += "(" + rule.start.getLine() + ":" + rule.start.getCharPositionInLine() + ")";
    }

    return nodeText;
  }

  private boolean treatAsTerminal(String ruleText) {
    return treatAsTerminal.contains(ruleText);
  }

  private StringBuilder addIndent(StringBuilder buf, int indent) {
    if (indent > 0)
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
    for (int i = from; i < to; i++) {
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
      if (value == lookFor)
        return true;
    }
    return false;
  }

}

class PublicLessG4Parser extends LessG4Parser {

  public PublicLessG4Parser(TokenStream input) {
    super(input);
  }

  public PredictionContextCache getChache() {
    return _sharedContextCache;
  }

  public Map<PredictionContext, PredictionContext> getMap() {
    return (new PublicPredictionContextCache(getChache())).getMap();
  }

  public DFA[] getArray() {
    return _decisionToDFA;
  }
}