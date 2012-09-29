package com.github.less4j.grammar;

import static com.github.less4j.grammar.GrammarAsserts.assertChilds;
import static com.github.less4j.grammar.GrammarAsserts.assertValid;
import static com.github.less4j.grammar.GrammarAsserts.assertValidSelector;

import org.junit.Test;
import org.porting.less4j.core.parser.LessLexer;

import com.github.less4j.core.parser.ANTLRParser;
import com.github.less4j.core.parser.ANTLRParser.ParseResult;

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
 */
public class SelectorsGrammarTest {

  @Test
  public void afterCSS2() {
    ANTLRParser compiler = new ANTLRParser();
    ANTLRParser.ParseResult result = compiler.parseSelector("li:after {");
    assertValidSelector(result);
    assertChilds(result.getTree(), LessLexer.EMPTY_COMBINATOR, LessLexer.ELEMENT_NAME, LessLexer.EMPTY_COMBINATOR, LessLexer.ELEMENT_SUBSEQUENT);
  }

  @Test
  public void afterCSS3() {
    ANTLRParser compiler = new ANTLRParser();
    String selector = "li::after {";
    ANTLRParser.ParseResult result = compiler.parseSelector(selector);
    assertValidSelector(result);
    assertChilds(result.getTree(), LessLexer.EMPTY_COMBINATOR, LessLexer.ELEMENT_NAME, LessLexer.EMPTY_COMBINATOR, LessLexer.ELEMENT_SUBSEQUENT);
  }

  @Test
  public void notPseudo() {
    ANTLRParser compiler = new ANTLRParser();
    String selector = "li:not(:only-child) { ";
    ANTLRParser.ParseResult result = compiler.parseSelector(selector);
    assertValidSelector(result);
    }

  @Test
  public void notNumber() {
    ANTLRParser compiler = new ANTLRParser();
    ANTLRParser.ParseResult result = compiler.parseSelector("class#id[attr=32]:not(1) {");
    assertValidSelector(result);
  }

  @Test
  public void notAttribute() {
    ANTLRParser compiler = new ANTLRParser();
    ANTLRParser.ParseResult result = compiler.parseSelector("p:not([class*=\"lead\"]) {");
    assertValidSelector(result);
  }
  
  @Test
  public void stylesheetWithMultipleFormulas() {
    String combined_stylesheet = "li:nth-child(4n+1),\n" +
        "li:nth-child(-5n),\n" +
        "li:nth-child(-n+2) {\n" +
        "  color: white;\n" +
        "}";
    
    ANTLRParser compiler = new ANTLRParser();
    ParseResult result = compiler.parseStyleSheet(combined_stylesheet);
    assertValid(result);
  }

  @Test
  public void formulaAnb() {
    ANTLRParser compiler = new ANTLRParser();
    ANTLRParser.ParseResult result = compiler.parseSelector("li:nth-child(4n+1) {");
    assertValidSelector(result);
  }

  @Test
  public void formulaAnMinusb() {
    ANTLRParser compiler = new ANTLRParser();
    ANTLRParser.ParseResult result = compiler.parseSelector("li:nth-child(4n-1) {");
    assertValidSelector(result);
  }

  @Test
  public void formulaAn() {
    ANTLRParser compiler = new ANTLRParser();
    ANTLRParser.ParseResult result = compiler.parseSelector("li:nth-child(4n) {");
    assertValidSelector(result);
  }

  @Test
  public void formulaMinusAn() {
    ANTLRParser compiler = new ANTLRParser();
    ANTLRParser.ParseResult result = compiler.parseSelector("li:nth-child(-4n) {");
    assertValidSelector(result);
  }

  @Test
  public void formulaMinusN() {
    ANTLRParser compiler = new ANTLRParser();
    ANTLRParser.ParseResult result = compiler.parseSelector("li:nth-child(-n) {");
    assertValidSelector(result);
  }

  @Test
  public void formulaMinusNMinus() {
    ANTLRParser compiler = new ANTLRParser();
    String selector = "li:nth-child(-n+2) {";
    ANTLRParser.ParseResult result = compiler.parseSelector(selector);
    assertValidSelector(result);
  }

  @Test
  public void formulaEven() {
    ANTLRParser compiler = new ANTLRParser();
    ANTLRParser.ParseResult result = compiler.parseSelector("li:nth-child(even) {");
    assertValidSelector(result);
  }

  @Test
  public void formulaEvenUppercase() {
    ANTLRParser compiler = new ANTLRParser();
    ANTLRParser.ParseResult result = compiler.parseSelector("li:NTH-CHILD(EVEN) {");
    assertValidSelector(result);
  }

  @Test
  public void formulaOdd() {
    ANTLRParser compiler = new ANTLRParser();
    ANTLRParser.ParseResult result = compiler.parseSelector("li:nth-child(odd) {");
    assertValidSelector(result);
  }

  @Test
  public void formulaOddUppercase() {
    ANTLRParser compiler = new ANTLRParser();
    ANTLRParser.ParseResult result = compiler.parseSelector("li:NTH-CHILD(ODD) {");
    assertValidSelector(result);
  }

}
