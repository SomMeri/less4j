package org.porting.less4j.grammar;

import static org.porting.less4j.grammar.GrammarAsserts.assertChilds;
import static org.porting.less4j.grammar.GrammarAsserts.assertValidExpression;

import org.junit.Test;
import org.porting.less4j.core.parser.ANTLRParser;
import org.porting.less4j.core.parser.LessLexer;

public class TermGrammarTest {

  @Test
  public void blah() {
    ANTLRParser compiler = new ANTLRParser();
    ANTLRParser.ParseResult result = compiler.parseExpression("aaa bbb");
    assertValidExpression(result);
    assertChilds(result.getTree(), LessLexer.TERM, LessLexer.TERM);
  }

}
