package org.porting.less4j.grammar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;

import org.antlr.runtime.tree.CommonTree;
import org.junit.Test;
import org.porting.less4j.core.ASTParser;
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
public class ExpressionGrammarTest {

  @Test
  public void sequenceNumbersLong() {
    ASTParser compiler = new ASTParser();
    CommonTree tree = compiler.compileExpression("12-34 56+7 8*90 ");
    assertValidExpression(compiler, tree);
    assertChilds(tree, LessLexer.NUMBER, LessLexer.MINUS, LessLexer.NUMBER, LessLexer.EMPTY_SEPARATOR, LessLexer.NUMBER, LessLexer.PLUS, LessLexer.NUMBER, LessLexer.EMPTY_SEPARATOR, LessLexer.NUMBER, LessLexer.STAR, LessLexer.NUMBER);
  }

  @Test
  public void sequenceNumbersLongSpaces() {
    ASTParser compiler = new ASTParser();
    CommonTree tree = compiler.compileExpression("12 - 34 56 + 7 8 * 90 ");
    assertValidExpression(compiler, tree);
    assertChilds(tree, LessLexer.NUMBER, LessLexer.MINUS, LessLexer.NUMBER, LessLexer.EMPTY_SEPARATOR, LessLexer.NUMBER, LessLexer.PLUS, LessLexer.NUMBER, LessLexer.EMPTY_SEPARATOR, LessLexer.NUMBER, LessLexer.STAR, LessLexer.NUMBER);
  }

  @Test
  public void sequencePixels() {
    ASTParser compiler = new ASTParser();
    CommonTree tree = compiler.compileExpression("12px 13px");
    assertValidExpression(compiler, tree);
    assertChilds(tree, LessLexer.LENGTH, LessLexer.EMPTY_SEPARATOR, LessLexer.LENGTH);
  }

  @Test
  public void plusPixels() {
    ASTParser compiler = new ASTParser();
    CommonTree tree = compiler.compileExpression("12px + 13px");
    assertValidExpression(compiler, tree);
    assertChilds(tree, LessLexer.LENGTH, LessLexer.PLUS, LessLexer.LENGTH);
  }

  @Test
  public void plusPixelsShort() {
    ASTParser compiler = new ASTParser();
    CommonTree tree = compiler.compileExpression("12px+13px");
    assertValidExpression(compiler, tree);
    assertChilds(tree, LessLexer.LENGTH, LessLexer.PLUS, LessLexer.LENGTH);
  }

  @Test
  public void sequenceNumbers() {
    ASTParser compiler = new ASTParser();
    CommonTree tree = compiler.compileExpression("1.2 13 ");
    assertValidExpression(compiler, tree);
    assertChilds(tree, LessLexer.NUMBER, LessLexer.EMPTY_SEPARATOR, LessLexer.NUMBER);
  }

  @Test
  public void floating() {
    ASTParser compiler = new ASTParser();
    CommonTree tree = compiler.compileExpression("1.2 ");

    assertValidExpression(compiler, tree);
    assertEquals(1, tree.getChildren().size());
    assertEquals(LessLexer.NUMBER, tree.getChild(0).getType());
  }

  @Test
  public void integer() {
    ASTParser compiler = new ASTParser();
    CommonTree tree = compiler.compileExpression("12 ");

    assertValidExpression(compiler, tree);
    assertEquals(1, tree.getChildren().size());
    assertEquals(LessLexer.NUMBER, tree.getChild(0).getType());
  }

  private void assertValidExpression(ASTParser compiler, CommonTree tree) {
    assertTrue(compiler.getAllErrors().isEmpty());
    assertEquals(LessLexer.EXPRESSION, tree.getType());
  }

  @SuppressWarnings("rawtypes")
  private void assertChilds(CommonTree tree, int... childType) {
    Iterator kids = tree.getChildren().iterator();
    for (int type : childType) {
      if (!kids.hasNext())
        fail("Some children are missing.");

      CommonTree kid = (CommonTree) kids.next();
      assertEquals(type, kid.getType());
    }

  }

}
