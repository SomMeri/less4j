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
 * Testing selectors parser.
 * 
 * Note: strange things happen with EOF during unit tests, so I added a dummy
 * { to the test selectors. The generated lexer thinks that there should be 
 * something after selector. That is true, but I want to test selector rule only. 
 *
 * Note: CSS3 pseudoclasses and pseudoelements are not properly handled for now: 
 * * https://developer.mozilla.org/en/CSS/:after
 * * selectors grammar http://www.w3.org/TR/css3-selectors/#gen-content
 * * http://www.w3.org/wiki/CSS3/Selectors#Pseudo-classes
 * * http://www.w3.org/TR/selectors/
 * 
 * TODO: those :nth-of-type() type selectors are not covered by the grammar
 * 
 */
public class SelectorsGrammarTest {

  @Test
  public void afterCSS2() {
    ASTParser compiler = new ASTParser();
    CommonTree tree = compiler.compileSelector("li:after {");
    assertValidSelector(compiler, tree);
    assertChilds(tree, LessLexer.IDENT, LessLexer.COLON, LessLexer.IDENT);
  }

  @Test
  public void afterCSS3() {
    ASTParser compiler = new ASTParser();
    CommonTree tree = compiler.compileSelector("li::after {");
    assertValidSelector(compiler, tree);
    assertChilds(tree, LessLexer.IDENT, LessLexer.COLON, LessLexer.COLON, LessLexer.IDENT);
  }

  @Test
  public void notPseudo() {
    ASTParser compiler = new ASTParser();
    CommonTree tree = compiler.compileSelector("li:not(:only-child) { ");
    assertValidSelector(compiler, tree);
    //TODO test also structure (once we decide what is it going to be)
    }

  @Test
  public void notNumber() {
    ASTParser compiler = new ASTParser();
    CommonTree tree = compiler.compileSelector("class#id[attr=32]:not(1) {");
    assertValidSelector(compiler, tree);
    //TODO test also structure (once we decide what is it going to be)
  }

  @Test
  public void notAttribute() {
    ASTParser compiler = new ASTParser();
    CommonTree tree = compiler.compileSelector("p:not([class*=\"lead\"]) {");
    assertValidSelector(compiler, tree);
    //TODO test also structure (once we decide what is it going to be)
  }

  private void assertValidSelector(ASTParser compiler, CommonTree tree) {
    assertTrue(compiler.getAllErrors().isEmpty());
    assertEquals(LessLexer.SELECTOR, tree.getType());
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
