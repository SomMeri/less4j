package org.porting.less4j.grammar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonErrorNode;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.TreeVisitor;
import org.antlr.runtime.tree.TreeVisitorAction;
import org.porting.less4j.core.parser.ANTLRParser;
import org.porting.less4j.core.parser.LessLexer;

public class GrammarAsserts {

  //TODO move to some helper
  public static final Set<Integer> DUMMY_TOKENS = new HashSet<Integer>(Arrays.asList(LessLexer.SELECTOR, LessLexer.RULESET,LessLexer.EMPTY_COMBINATOR,LessLexer.EMPTY_SEPARATOR, LessLexer.EXPRESSION ));

  public static void assertValidSelector(ANTLRParser.ParseResult result) {
    assertValid(result);
    assertEquals(LessLexer.SELECTOR, result.getTree().getType());
  }

  public static void assertValidExpression(ANTLRParser.ParseResult result) {
    assertValid(result);
    assertEquals(LessLexer.EXPRESSION, result.getTree().getType());
  }

  public static void assertValidTerm(ANTLRParser.ParseResult result) {
    assertValid(result);
    assertEquals(LessLexer.TERM, result.getTree().getType());
  }

  public static void assertValid(ANTLRParser.ParseResult result) {
    assertTrue(result.getErrors().isEmpty());
  }

  @SuppressWarnings("rawtypes")
  public static void assertChilds(CommonTree tree, int... childType) {
    Iterator kids = tree.getChildren().iterator();
    for (int type : childType) {
      if (!kids.hasNext())
        fail("Some children are missing.");

      CommonTree kid = (CommonTree) kids.next();
      assertEquals(type, kid.getType());
    }

  }

  public static void assertNoTokenMissing(String crashingSelector, CommonTree tree) {
    CommonTokenStream tokens = createTokenStream(crashingSelector);
    int expectedNumberOfTokens = countOnChannelTokes(tokens);
    int treeTokesCount = countAllTreeTokens(tokens, tree);
    assertEquals(expectedNumberOfTokens, treeTokesCount);
  }

  private static int countOnChannelTokes(CommonTokenStream tokens) {
    int numberOfOnChannelTokens = tokens.getNumberOfOnChannelTokens();
    //the above number includes also EOF, we substract that
    return numberOfOnChannelTokens - 1;
  }

  private static int countAllTreeTokens(CommonTokenStream tokens, CommonTree tree) {
    CountNodesAction action = new CountNodesAction(tokens);
    TreeVisitor visitor = new TreeVisitor();
    visitor.visit(tree, action);
    return action.getCount();
  }

  private static CommonTokenStream createTokenStream(String text) {
    ANTLRStringStream input = new ANTLRStringStream(text);
    LessLexer lexer = new LessLexer(input);
    return new CommonTokenStream(lexer);
  }

}

class CountNodesAction implements TreeVisitorAction {

  private int count = 0;
  private final CommonTokenStream tokens;
  
  public CountNodesAction(CommonTokenStream tokens) {
    super();
    this.tokens = tokens;
  }

  @Override
  public Object pre(Object t) {
    if (t instanceof CommonErrorNode) {
      CommonErrorNode errorNode = (CommonErrorNode)t;
      int startIndex = errorNode.start.getTokenIndex();
      int stopIndex = errorNode.stop.getTokenIndex();
      count+=countOnChannelTokes(startIndex, stopIndex);
    } else {
      if (!isDummy(((CommonTree)t).getToken()))
        count++;
    }
    return t;
  }

  public int getCount() {
    return count;
  }

  @Override
  public Object post(Object t) {
    return t;
  }

  private int countOnChannelTokes(int start, int end) {
    @SuppressWarnings("unchecked")
    List<CommonToken> list = tokens.get(start, end);
    int count = 0;
    for (CommonToken token : list) {
      if (isOnChannel(token) && !isDummy(token))
        count++;
    }
    return count;
  }

  //TODO move to some helper
  private boolean isDummy(Token token) {
    return token.getType()<LessLexer.CHARSET_SYM;
  }

  private boolean isOnChannel(CommonToken token) {
    return token.getChannel()==Token.DEFAULT_CHANNEL;
  }

}
