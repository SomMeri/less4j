package org.porting.less4j.grammar;

import static org.porting.less4j.grammar.GrammarAsserts.assertNoTokenMissing;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.junit.Test;
import org.porting.less4j.core.parser.ANTLRParser;
import org.porting.less4j.core.parser.HiddenTokenAwareTree;
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
    ANTLRParser compiler = new ANTLRParser();
    CommonTree tree = compiler.parseSelector(crashingSelector);
    
    assertNoTokenMissing(crashingSelector, tree);
  }

  @Test
  public void combinedIncorrectSelector() throws RecognitionException {
    String crashingSelector = "p:not*()  p:not()";
    ANTLRParser compiler = new ANTLRParser();
    CommonTree tree = compiler.parseSelector(crashingSelector);

    assertNoTokenMissing(crashingSelector, tree);
  }

  @Test
  public void stylesheet() throws RecognitionException {
    String crashingSelector =   "p:not([class**=\"lead\"]) {\n  color: black;\n}";
    ANTLRParser compiler = new ANTLRParser();
    HiddenTokenAwareTree tree = compiler.parse(crashingSelector);

    DebugPrint.printTokenStream(crashingSelector);
    DebugPrint.print(tree);
    assertNoTokenMissing(crashingSelector, tree);
  }


}

