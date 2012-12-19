package com.github.sommeri.less4j.core.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.antlr.runtime.Parser;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;

import com.github.sommeri.less4j.LessCompiler.Problem;
import com.github.sommeri.less4j.core.parser.ParsersSemanticPredicates;
import com.github.sommeri.less4j.core.parser.AntlrException;

public class SuperLessParser extends Parser {

  public final String[] tokenErrorNames;
  //TODO: write about this to error handling post, it would be cool if I could do this declaratively in grammar 
  private static final Map<String, String> ALTERNATIVE_NAMES_FOR_ERROR_REPORTING = new HashMap<String, String>();
  static {
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("CHARSET_SYM", "@charset");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("STRING", "string");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("SEMI", ";");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("IMPORT_SYM", "@import");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("URI", "url(...)");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("COMMA", ",");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("MEDIA_SYM", "@media");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("LBRACE", "{");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("RBRACE", "}");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("IDENT", "identifier");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("LPAREN", "(");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("COLON", ":");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("RPAREN", ")");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("VARIABLE", "@<variable name>");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("DOT3", "...");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("INDIRECT_VARIABLE", "@@<variable name>");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("FONT_FACE_SYM", "@font-face");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("PAGE_SYM", "@page");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("GREATER", ">");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("PLUS", "+");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("TILDE", "~");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("MINUS", "-");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("APPENDER", "&");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("HASH", "#name");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("DOT", ".");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("LBRACKET", "[");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("NUMBER", "number");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("STAR", "repeater");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("OPEQ", "*");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("INCLUDES", "=");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("DASHMATCH", "~=");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("PREFIXMATCH", "|=");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("SUFFIXMATCH", "^=");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("SUBSTRINGMATCH", "$=");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("RBRACKET", "*=");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("IMPORTANT_SYM", "]");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("GREATER_OR_EQUAL", "!important");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("LOWER_OR_EQUAL", ">=");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("LOWER", "<=");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("SOLIDUS", "<");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("EMPTY_COMBINATOR", "/");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("URL", "empty combinator");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("NL", "url");
    ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.put("NEW_LINE", "new line");
  }

  protected static final String RULE_STYLESHEET = "stylesheet";
  protected static final String RULE_FONT_FACE = "font-face";
  protected static final String RULE_MEDIA = "media";
  protected static final String RULE_NAMESPACE_REFERENCE = "namespace reference";
  protected static final String RULE_VARIABLE_REFERENCE = "variable reference";
  protected static final String RULE_VARIABLE_DECLARATION = "variable declaration";
  protected static final String RULE_MIXIN_REFERENCE = "mixin reference";
  protected static final String RULE_ABSTRACT_MIXIN_OR_NAMESPACE = "abstract mixin or namespace";
  protected static final String RULE_PSEUDO_PAGE = "pseudo page";
  protected static final String RULE_DECLARATION = "declaration";
  protected static final String RULE_EXPRESSION = "expression";
  protected static final String RULE_IMPORTS = "imports";
  protected static final String RULE_CHARSET = "charset";
  protected static final String RULE_PAGE = "page";
  protected static final String RULE_RULESET = "ruleset";
  protected static final String RULE_SELECTOR = "selectors";
  protected static final String RULE_MEDIUM = "medium";

  protected List<Problem> errors = new ArrayList<Problem>();
  protected ParsersSemanticPredicates predicates = new ParsersSemanticPredicates();
  protected Stack<EnterRuleInfo> paraphrases = new Stack<EnterRuleInfo>();

  public SuperLessParser(TokenStream input, List<Problem> errors) {
    this(input, new RecognizerSharedState(), errors);
  }

  public SuperLessParser(TokenStream input, RecognizerSharedState state, List<Problem> errors) {
    super(input, state);
    this.errors = errors;
    this.tokenErrorNames = generateTokenErrorNames();
  }

  private String[] generateTokenErrorNames() {
    String[] result = getTokenNames().clone();
    for (int i = 0; i < result.length; i++) {
      if (ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.containsKey(result[i])) {
        result[i] = "'" + ALTERNATIVE_NAMES_FOR_ERROR_REPORTING.get(result[i]) + "'";
      }
    }
    return result;
  }

  public SuperLessParser(TokenStream input, RecognizerSharedState state) {
    this(input, state, new ArrayList<Problem>());
  }

  public List<Problem> getAllErrors() {
    return new ArrayList<Problem>(errors);
  }

  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  public void reportError(RecognitionException e) {
    errors.add(new AntlrException(e, getErrorMessage(e, tokenErrorNames)));
  }

  public String getErrorHeader(RecognitionException e) {
    if (getSourceName() != null)
      return getSourceName() + " line " + e.line + ":" + (e.charPositionInLine + 1);

    return "line " + e.line + ":" + (e.charPositionInLine + 1);
  }

  @Override
  public String getErrorMessage(RecognitionException e, String[] tokenNames) {
    String result = "" + super.getErrorMessage(e, tokenNames);

    if (!paraphrases.isEmpty()) {
      EnterRuleInfo info = paraphrases.peek();
      String position = info.getStart().getLine() + ":" + (info.getStart().getCharPositionInLine() + 1);
      result = result + " in " + info.getRulename() + " (which started at " + position + ")";
    }
    return result;
  }

  protected void enterRule(ParserRuleReturnScope retval, String rulename) {
    //the init block is done always but the after block is done only if the thing is not backtracking 
    paraphrases.add(new EnterRuleInfo(retval.start, rulename));
  }

  protected void leaveRule() {
    paraphrases.pop();
  }
}

class EnterRuleInfo {

  private final Token start;
  private final String rulename;

  public EnterRuleInfo(Token start, String rulename) {
    this.start = start;
    this.rulename = rulename;
  }

  public Token getStart() {
    return start;
  }

  public String getRulename() {
    return rulename;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("[");
    builder.append(rulename);
    builder.append("]");
    return builder.toString();
  }

}
