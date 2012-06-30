package org.porting.less4j.grammar;

import static org.porting.less4j.grammar.GrammarAsserts.assertChilds;
import static org.porting.less4j.grammar.GrammarAsserts.assertValid;
import static org.porting.less4j.grammar.GrammarAsserts.assertValidSelector;

import org.antlr.runtime.tree.CommonTree;
import org.junit.Test;
import org.porting.less4j.core.parser.ANTLRParser;
import org.porting.less4j.core.parser.LessLexer;
import org.porting.less4j.debugutils.DebugPrint;

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
 * TODO add tests for what is in this document: http://www.w3.org/TR/selectors/#nth-child-pseudo
 * 
 */
public class SelectorsGrammarTest {

  @Test
  public void afterCSS2() {
    ANTLRParser compiler = new ANTLRParser();
    CommonTree tree = compiler.parseSelector("li:after {");
    assertValidSelector(compiler, tree);
    assertChilds(tree, LessLexer.SIMPLE_SELECTOR);
    assertChilds((CommonTree)tree.getChild(0), LessLexer.ELEMENT_NAME, LessLexer.PSEUDO);
  }

  @Test
  public void afterCSS3() {
    ANTLRParser compiler = new ANTLRParser();
    String selector = "li::after {";
    CommonTree tree = compiler.parseSelector(selector);
    assertValidSelector(compiler, tree);
    DebugPrint.print(tree);
    assertChilds(tree, LessLexer.SIMPLE_SELECTOR);
    assertChilds((CommonTree)tree.getChild(0), LessLexer.ELEMENT_NAME, LessLexer.PSEUDO);
  }

  @Test
  public void notPseudo() {
    ANTLRParser compiler = new ANTLRParser();
    String selector = "li:not(:only-child) { ";
    CommonTree tree = compiler.parseSelector(selector);
    assertValidSelector(compiler, tree);
    }

  @Test
  public void notNumber() {
    ANTLRParser compiler = new ANTLRParser();
    CommonTree tree = compiler.parseSelector("class#id[attr=32]:not(1) {");
    assertValidSelector(compiler, tree);
  }

  @Test
  public void notAttribute() {
    ANTLRParser compiler = new ANTLRParser();
    CommonTree tree = compiler.parseSelector("p:not([class*=\"lead\"]) {");
    assertValidSelector(compiler, tree);
  }
  
  @Test
  public void stylesheetWithMultipleFormulas() {
    String combined_stylesheet = "li:nth-child(4n+1),\n" +
        "li:nth-child(-5n),\n" +
        "li:nth-child(-n+2) {\n" +
        "  color: white;\n" +
        "}";
    
    ANTLRParser compiler = new ANTLRParser();
    compiler.parse(combined_stylesheet);
    assertValid(compiler);
  }

  @Test
  public void formulaAnb() {
    ANTLRParser compiler = new ANTLRParser();
    CommonTree tree = compiler.parseSelector("li:nth-child(4n+1) {");
    assertValidSelector(compiler, tree);
  }

  @Test
  public void formulaAnMinusb() {
    ANTLRParser compiler = new ANTLRParser();
    CommonTree tree = compiler.parseSelector("li:nth-child(4n-1) {");
    assertValidSelector(compiler, tree);
  }

  @Test
  public void formulaAn() {
    ANTLRParser compiler = new ANTLRParser();
    CommonTree tree = compiler.parseSelector("li:nth-child(4n) {");
    assertValidSelector(compiler, tree);
  }

  @Test
  public void formulaMinusAn() {
    ANTLRParser compiler = new ANTLRParser();
    CommonTree tree = compiler.parseSelector("li:nth-child(-4n) {");
    assertValidSelector(compiler, tree);
  }

  @Test
  public void formulaMinusN() {
    ANTLRParser compiler = new ANTLRParser();
    CommonTree tree = compiler.parseSelector("li:nth-child(-n) {");
    assertValidSelector(compiler, tree);
  }

  @Test
  public void formulaMinusNMinus() {
    ANTLRParser compiler = new ANTLRParser();
    String selector = "li:nth-child(-n+2) {";
    DebugPrint.printTokenStream(selector);
    CommonTree tree = compiler.parseSelector(selector);
    assertValidSelector(compiler, tree);
  }

  @Test
  public void formulaEven() {
    ANTLRParser compiler = new ANTLRParser();
    //TODO ako je to s case sensitivity?
    CommonTree tree = compiler.parseSelector("li:nth-child(even) {");
    assertValidSelector(compiler, tree);
  }

  //FIXME: what happens when somebody names a class ODD or EVEN? it is quite likely,
  //especially in CSS2
  //TODO what does less.js do in this situation: nth-child(@variable+4) ???
  @Test
  public void formulaOdd() {
    ANTLRParser compiler = new ANTLRParser();
    //TODO ako je to s case sensitivity?
    CommonTree tree = compiler.parseSelector("li:nth-child(odd) {");
    assertValidSelector(compiler, tree);
  }

}
