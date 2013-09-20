package com.github.sommeri.less4j.compiler;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.github.sommeri.less4j.core.parser.LessParser;

public class TokenNamesForErrorReportingTest {

  private static final List<String> TECHNICAL_ANTLR = Arrays.asList("<invalid>", "<EOR>", "<DOWN>", "<UP>");
  private static final List<String> FICTIONAL = Arrays.asList("ARGUMENT_DECLARATION", "VARIABLE_DECLARATION", "ARGUMENT_COLLECTOR", "EXPRESSION", "DECLARATION", "VARIABLE_REFERENCE", "RULESET", "NESTED_APPENDER", "EXTENDED_SELECTOR", "SELECTOR", "SIMPLE_SELECTOR", "ESCAPED_SELECTOR", "EXPRESSION_PARENTHESES", "ESCAPED_VALUE", "STYLE_SHEET", "EMPTY_SEPARATOR", "ELEMENT_NAME", "CSS_CLASS", "NTH", "PSEUDO", "ATTRIBUTE", "ID_SELECTOR", "ELEMENT_SUBSEQUENT", "CHARSET_DECLARATION", "TERM_FUNCTION", "TERM", "MEDIUM_DECLARATION", "FIXED_MEDIA_EXPRESSION", "MEDIA_QUERY", "MEDIUM_TYPE", "BODY", "MIXIN_REFERENCE", "NAMESPACE_REFERENCE", "REUSABLE_STRUCTURE", "MIXIN_PATTERN", "GUARD_CONDITION", "GUARD", "DUMMY_MEANINGFULL_WHITESPACE", "KEYFRAMES", "DOCUMENT_DECLARATION", "REUSABLE_STRUCTURE_NAME", "VIEWPORT");
  private static final List<String> TECHNICAL_LESS4J = Arrays.asList("SEMI_SPLIT_MIXIN_DECLARATION_ARGUMENTS", "SEMI_SPLIT_MIXIN_REFERENCE_ARGUMENTS", "UNICODE_RANGE_HEX","TERM_FUNCTION_NAME", "NMSTART", "NMCHAR", "NAME", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");

  @Test
  public void allReevantTokensHaveNames() {
    String[] tokenNames = LessParser.tokenNames;
    Set<String> coveredTokens = LessParser.ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.keySet();

    List<String> missing = new ArrayList<String>(Arrays.asList(tokenNames));
    missing.removeAll(coveredTokens);
    missing.removeAll(TECHNICAL_ANTLR);
    missing.removeAll(FICTIONAL);
    missing.removeAll(TECHNICAL_LESS4J);

    if (!missing.isEmpty()) {
      for (String string : missing) {
        System.err.println(string);
      }
      fail("Some tokens are missing alternative error names.");
    }
  }

  @Test
  public void noOldTokensRenaimedNames() {
    String[] tokenNames = LessParser.tokenNames;
    Set<String> coveredTokens = LessParser.ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.keySet();

    List<String> additional = new ArrayList<String>(coveredTokens);
    additional.removeAll(Arrays.asList(tokenNames));
    if (!additional.isEmpty()) {
      for (String string : additional) {
        System.err.println(string);
      }
      fail("Removed tokens remained in alternatives names set.");
    }

  }

}
