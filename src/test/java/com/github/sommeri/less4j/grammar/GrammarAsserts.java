package com.github.sommeri.less4j.grammar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.List;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.TreeVisitor;
import org.antlr.runtime.tree.TreeVisitorAction;

import com.github.sommeri.less4j.core.parser.LessLexer;
import com.github.sommeri.less4j.core.parser.ANTLRParser;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareErrorTree;
import com.github.sommeri.less4j.utils.PrintUtils;

public class GrammarAsserts {

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
      assertEquals("Should be: " + PrintUtils.toName(type)+" was: "+ PrintUtils.toName(kid.getType()), type, kid.getType());
    }

  }

  public static void assertNoTokenMissing(String crashingSelector, CommonTree tree) {
    assertNoTokenMissing(crashingSelector, tree, 0);
  }

  public static void assertNoTokenMissing(String crashingSelector, CommonTree tree, int expectedDummies) {
    CommonTokenStream tokens = createTokenStream(crashingSelector);
    int onChannelTokens = countOnChannelTokes(tokens);
    int treeTokesCount = countAllTreeTokens(tokens, tree);
    assertEquals(onChannelTokens + expectedDummies, treeTokesCount);
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
    if (t instanceof HiddenTokenAwareErrorTree) {
      HiddenTokenAwareErrorTree errorNode = (HiddenTokenAwareErrorTree)t;
      int startIndex = errorNode.getStart().getTokenIndex();
      int stopIndex = errorNode.getStop().getTokenIndex();
      int errorTokens = countOnChannelTokes(startIndex, stopIndex);
      count+=errorTokens;
//      System.out.println("Error tokens " + errorTokens);
    } else {
      if (!isDummy(((CommonTree)t).getToken())) {
        count++;
//        String string = DebugAndTestPrint.toString(count, ((CommonTree)t).getToken());
//        System.out.println(string);
      }
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
    List<? extends Token> list = tokens.get(start, end);
    int count = 0;
    for (Token token : list) {
      CommonToken commonToke = CommonToken.class.cast(token);
      if (isOnChannel(commonToke) && !isDummy(commonToke))
        count++;
    }
    return count;
  }

  private boolean isDummy(Token token) {
    return token.getType()<LessLexer.AT_NAME;
  }

  private boolean isOnChannel(CommonToken token) {
    return token.getChannel()==Token.DEFAULT_CHANNEL;
  }

}
