package org.porting.less4j.grammar;

import static org.porting.less4j.grammar.GrammarAsserts.assertNoTokenMissing;

import org.antlr.runtime.RecognitionException;
import org.junit.Test;
import org.porting.less4j.core.parser.ANTLRParser;
import org.porting.less4j.core.parser.ANTLRParser.ParseResult;
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
    ParseResult result = compiler.parseSelector(crashingSelector);
    
    assertNoTokenMissing(crashingSelector, result.getTree());
  }

  @Test
  public void combinedIncorrectSelector() throws RecognitionException {
    String crashingSelector = "p:not*()  p:not()";
    ANTLRParser compiler = new ANTLRParser();
    ParseResult result = compiler.parseSelector(crashingSelector);
    DebugPrint.printTokenStream(crashingSelector);

    assertNoTokenMissing(crashingSelector, result.getTree());
  }

  @Test
  public void stylesheet() throws RecognitionException {
    String crashingSelector =   "p:not([class**=\"lead\"]) {\n  color: black;\n}";
    ANTLRParser compiler = new ANTLRParser();
    ParseResult result = compiler.parseStyleSheet(crashingSelector);

    DebugPrint.printTokenStream(crashingSelector);
    DebugPrint.print(result.getTree());
    assertNoTokenMissing(crashingSelector, result.getTree());
  }


}

