package org.porting.less4j.grammar;

import static org.junit.Assert.assertEquals;
import static org.porting.less4j.grammar.GrammarAsserts.assertChilds;
import static org.porting.less4j.grammar.GrammarAsserts.assertValidExpression;

import org.antlr.runtime.tree.CommonTree;
import org.junit.Ignore;
import org.junit.Test;
import org.porting.less4j.core.parser.ANTLRParser;
import org.porting.less4j.core.parser.LessLexer;

/**
 * Testing numbers and arithmetic expression parser.
 * 
 * Note: strange things happen with EOF during unit tests, so I added a dummy
 * space to the test expressions.
 * 
 * Found something about EOF:
 * http://www.antlr.org/pipermail/antlr-interest/2009-January/032219.html
 * 
 */
//the test is ignored. I will do pure css first and add other features later
@Ignore
public class ExpressionGrammarTest {

  @Test
  public void sequenceNumbersLong() {
    ANTLRParser compiler = new ANTLRParser();
    CommonTree tree = compiler.parseExpression("12-34 56+7 8*90 ");
    assertValidExpression(compiler, tree);
    assertChilds(tree, LessLexer.NUMBER, LessLexer.MINUS, LessLexer.NUMBER, LessLexer.EMPTY_SEPARATOR, LessLexer.NUMBER, LessLexer.PLUS, LessLexer.NUMBER, LessLexer.EMPTY_SEPARATOR, LessLexer.NUMBER, LessLexer.STAR, LessLexer.NUMBER);
  }

  @Test
  public void sequenceNumbersLongSpaces() {
    ANTLRParser compiler = new ANTLRParser();
    CommonTree tree = compiler.parseExpression("12 - 34 56 + 7 8 * 90 ");
    assertValidExpression(compiler, tree);
    assertChilds(tree, LessLexer.NUMBER, LessLexer.MINUS, LessLexer.NUMBER, LessLexer.EMPTY_SEPARATOR, LessLexer.NUMBER, LessLexer.PLUS, LessLexer.NUMBER, LessLexer.EMPTY_SEPARATOR, LessLexer.NUMBER, LessLexer.STAR, LessLexer.NUMBER);
  }

  @Test
  public void sequencePixels() {
    ANTLRParser compiler = new ANTLRParser();
    CommonTree tree = compiler.parseExpression("12px 13px");
    assertValidExpression(compiler, tree);
    assertChilds(tree, LessLexer.LENGTH, LessLexer.EMPTY_SEPARATOR, LessLexer.LENGTH);
  }

  @Test
  public void plusPixels() {
    ANTLRParser compiler = new ANTLRParser();
    CommonTree tree = compiler.parseExpression("12px + 13px");
    assertValidExpression(compiler, tree);
    assertChilds(tree, LessLexer.LENGTH, LessLexer.PLUS, LessLexer.LENGTH);
  }

  @Test
  public void plusPixelsShort() {
    ANTLRParser compiler = new ANTLRParser();
    CommonTree tree = compiler.parseExpression("12px+13px");
    assertValidExpression(compiler, tree);
    assertChilds(tree, LessLexer.LENGTH, LessLexer.PLUS, LessLexer.LENGTH);
  }

  @Test
  public void sequenceNumbers() {
    ANTLRParser compiler = new ANTLRParser();
    CommonTree tree = compiler.parseExpression("1.2 13 ");
    assertValidExpression(compiler, tree);
    assertChilds(tree, LessLexer.NUMBER, LessLexer.EMPTY_SEPARATOR, LessLexer.NUMBER);
  }

  @Test
  public void floating() {
    ANTLRParser compiler = new ANTLRParser();
    CommonTree tree = compiler.parseExpression("1.2 ");

    assertValidExpression(compiler, tree);
    assertEquals(1, tree.getChildren().size());
    assertEquals(LessLexer.NUMBER, tree.getChild(0).getType());
  }

  @Test
  public void integer() {
    ANTLRParser compiler = new ANTLRParser();
    CommonTree tree = compiler.parseExpression("12 ");

    assertValidExpression(compiler, tree);
    assertEquals(1, tree.getChildren().size());
    assertEquals(LessLexer.NUMBER, tree.getChild(0).getType());
  }

}
