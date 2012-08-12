package org.porting.less4j.grammar;

import org.junit.Test;
import org.porting.less4j.core.parser.ANTLRParser;
import org.porting.less4j.debugutils.DebugPrint;

/**
 * We need to get access to hidden tokens. 
 */
public class CommentsGrammarTest {

  @Test
  public void experiment() {
    ANTLRParser compiler = new ANTLRParser();
    String css = "/* comment*/ li:after {}";
    DebugPrint.printTokenStream(css);
    ANTLRParser.ParseResult result = compiler.parseStyleSheet(css);
    DebugPrint.print(result.getTree());
  }

}
