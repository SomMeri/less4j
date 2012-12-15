package com.github.sommeri.less4j.grammar;

import static com.github.sommeri.less4j.grammar.GrammarAsserts.assertNoTokenMissing;

import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import com.github.sommeri.less4j.core.parser.ANTLRParser;
import com.github.sommeri.less4j.core.parser.ANTLRParser.ParseResult;

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
    
    assertNoTokenMissing(crashingSelector, result.getTree(), 0);
  }

  @Test
  public void combinedIncorrectSelector() throws RecognitionException {
    String crashingSelector = "p:not*()  p:not() { }";
    ANTLRParser compiler = new ANTLRParser();
    ParseResult result = compiler.parseRuleset(crashingSelector);

    assertNoTokenMissing(crashingSelector, result.getTree(), -1);
  }

  @Test
  public void stylesheet() throws RecognitionException {
    String crashingSelector =   "p:not([class**=\"lead\"]) {\n  color: black;\n}";
    ANTLRParser compiler = new ANTLRParser();
    ParseResult result = compiler.parseStyleSheet(crashingSelector);

    //the -3 is correct, even if it seems like huge hack. It sort of is.
    //RBRACE }; LBRACE {; RBRACKET ] and LBRACKET [ are (correctly thrown away)
    //and one dummy node EMPTY_COMBINATOR is added during the translation.
    //therefore there are -3 dummy nodes
    //this way of testing malformed trees is bad anyway,this needs to be changed 
    //for something more readable and stable. (These tests are broken with each tree change)
    assertNoTokenMissing(crashingSelector, result.getTree(), -5);
  }


}

