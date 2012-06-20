package org.porting.less4j.grammar;

import static org.junit.Assert.fail;

import org.antlr.runtime.tree.CommonTree;
import org.junit.Test;
import org.porting.less4j.core.ASTParser;
import org.porting.less4j.debugutils.DebugPrint;

/**
 * We need to get access to hidden tokens. 
 */
public class CommentsGrammarTest {

  @Test
  public void experiment() {
    ASTParser compiler = new ASTParser();
    String css = "/* comment*/ li:after {}";
    DebugPrint.printTokenStream(css);
    CommonTree tree = compiler.compile(css);
    DebugPrint.print(tree);
  }

}
