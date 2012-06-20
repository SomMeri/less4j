package org.porting.less4j.grammar;

import static org.porting.less4j.grammar.GrammarAsserts.assertNoTokenMissing;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.junit.Test;
import org.porting.less4j.core.ASTParser;
import org.porting.less4j.debugutils.DebugPrint;

//TODO: comments
//TODO: once jUnit 11 comes out, rework tests like these to named repeaters
/**
 * Tests for malformed CSS. For now, we are just checking whether final tree does not miss
 * some tokens and whether the parser does not throw an exception.  
 */
public class MalformedCssGrammarTest {

  @Test
  public void simpleIncorrectSelector() throws RecognitionException {
    String crashingSelector = "p:not*()";
    ASTParser compiler = new ASTParser();
    CommonTree tree = compiler.compileSelector(crashingSelector);
    
    assertNoTokenMissing(crashingSelector, tree);
  }

  @Test
  public void combinedIncorrectSelector() throws RecognitionException {
    String crashingSelector = "p:not*()  p:not()";
    ASTParser compiler = new ASTParser();
    CommonTree tree = compiler.compileSelector(crashingSelector);

    assertNoTokenMissing(crashingSelector, tree);
  }

  @Test
  public void stylesheet() throws RecognitionException {
    String crashingSelector =   "p:not([class**=\"lead\"]) {\n  color: black;\n}";
    ASTParser compiler = new ASTParser();
    CommonTree tree = compiler.compile(crashingSelector);

    DebugPrint.printTokenStream(crashingSelector);
    DebugPrint.print(tree);
    assertNoTokenMissing(crashingSelector, tree);
  }


}

