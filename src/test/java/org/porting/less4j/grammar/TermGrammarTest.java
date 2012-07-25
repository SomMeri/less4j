package org.porting.less4j.grammar;

import static org.porting.less4j.grammar.GrammarAsserts.assertChilds;
import static org.porting.less4j.grammar.GrammarAsserts.assertValidExpression;

import org.antlr.runtime.tree.CommonTree;
import org.junit.Test;
import org.porting.less4j.core.parser.ANTLRParser;
import org.porting.less4j.core.parser.LessLexer;

public class TermGrammarTest {

  @Test
  public void blah() {
    ANTLRParser compiler = new ANTLRParser();
    CommonTree tree = compiler.parseExpression("aaa bbb");
    assertValidExpression(compiler, tree);
    assertChilds(tree, LessLexer.TERM, LessLexer.TERM);
  }

}
