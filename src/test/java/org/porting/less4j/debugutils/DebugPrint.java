package org.porting.less4j.debugutils;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.Token;
import org.porting.less4j.core.parser.HiddenTokenAwareTree;
import org.porting.less4j.core.parser.LessLexer;

public class DebugPrint {

  private static boolean printIndexes = true;
  private static boolean printComments = true;

  public static void printTokenStream(String expression) {
    LessLexer createLexer = createLexer(expression);
    Token token = createLexer.nextToken();
    int count = 0;
    while (token.getType() != LessLexer.EOF) {
      System.out.println(toString(count++, token));
      token = createLexer.nextToken();
    }
  }

  private static LessLexer createLexer(String expression) {
    ANTLRStringStream input = new ANTLRStringStream(expression);
    LessLexer lexer = new LessLexer(input);
    return lexer;
  }

  public static String toString(int count, Token token) {
    return "(" + count + ") " + toName(token.getType()) + " " + toOneLine(token.getText());
  }

  private static String toOneLine(String text) {
    return text.replace("\n", "");
  }

  private static String toName(int type) {
    // %s/\([_A-Z]*\)=.*/\1: return "\1";/g
    // if it has pattern case LessLexer.EOF
    switch (type) {
    case LessLexer.EOF:
      return "EOF";
    case LessLexer.EXPRESSION:
      return "EXPRESSION";
    case LessLexer.DECLARATION:
      return "DECLARATION";
    case LessLexer.RULESET:
      return "RULESET";
    case LessLexer.SELECTOR:
      return "SELECTOR";
    case LessLexer.STYLE_SHEET:
      return "STYLE_SHEET";
    case LessLexer.EMPTY_SEPARATOR:
      return "EMPTY_SEPARATOR";
    case LessLexer.ELEMENT_NAME:
      return "ELEMENT_NAME";
    case LessLexer.CSS_CLASS:
      return "CSS_CLASS";
    case LessLexer.PSEUDO:
      return "PSEUDO_CLASS";
    case LessLexer.ATTRIBUTE:
      return "ATTRIBUTE";
    case LessLexer.ID_SELECTOR:
      return "ID_SELECTOR";
    case LessLexer.CHARSET_DECLARATION:
      return "CHARSET_DECLARATION";
    case LessLexer.TERM_FUNCTION:
      return "TERM_FUNCTION";
    case LessLexer.TERM:
      return "TERM";
    case LessLexer.MEDIUM_DECLARATION:
      return "MEDIUM_DECLARATION";
    case LessLexer.BODY_OF_DECLARATIONS:
      return "BODY_OF_DECLARATIONS";
    case LessLexer.CHARSET_SYM:
      return "CHARSET_SYM";
    case LessLexer.STRING:
      return "STRING";
    case LessLexer.SEMI:
      return "SEMI";
    case LessLexer.IMPORT_SYM:
      return "IMPORT_SYM";
    case LessLexer.URI:
      return "URI";
    case LessLexer.COMMA:
      return "COMMA";
    case LessLexer.MEDIA_SYM:
      return "MEDIA_SYM";
    case LessLexer.LBRACE:
      return "LBRACE";
    case LessLexer.RBRACE:
      return "RBRACE";
    case LessLexer.IDENT:
      return "IDENT";
    case LessLexer.FONT_FACE_SYM:
      return "FONT_FACE_SYM";
    case LessLexer.PAGE_SYM:
      return "PAGE_SYM";
    case LessLexer.COLON:
      return "COLON";
    case LessLexer.SOLIDUS:
      return "SOLIDUS";
    case LessLexer.STAR:
      return "STAR";
    case LessLexer.PLUS:
      return "PLUS";
    case LessLexer.GREATER:
      return "CHILD";
    case LessLexer.EMPTY_COMBINATOR:
      return "EMPTY_COMBINATOR";
    case LessLexer.MINUS:
      return "MINUS";
    case LessLexer.HASH:
      return "HASH";
    case LessLexer.DOT:
      return "DOT";
    case LessLexer.LBRACKET:
      return "LBRACKET";
    case LessLexer.OPEQ:
      return "OPEQ";
    case LessLexer.INCLUDES:
      return "INCLUDES";
    case LessLexer.DASHMATCH:
      return "DASHMATCH";
    case LessLexer.PREFIXMATCH:
      return "PREFIXMATCH";
    case LessLexer.SUFFIXMATCH:
      return "SUFFIXMATCH";
    case LessLexer.SUBSTRINGMATCH:
      return "SUBSTRINGMATCH";
    case LessLexer.NUMBER:
      return "NUMBER";
    case LessLexer.RBRACKET:
      return "RBRACKET";
    case LessLexer.LPAREN:
      return "LPAREN";
    case LessLexer.RPAREN:
      return "RPAREN";
    case LessLexer.IMPORTANT_SYM:
      return "IMPORTANT_SYM";
    case LessLexer.PERCENTAGE:
      return "PERCENTAGE";
    case LessLexer.LENGTH:
      return "LENGTH";
    case LessLexer.EMS:
      return "EMS";
    case LessLexer.EXS:
      return "EXS";
    case LessLexer.ANGLE:
      return "ANGLE";
    case LessLexer.UNKNOWN_DIMENSION:
      return "UNKNOWN_DIMENSION";
    case LessLexer.TIME:
      return "TIME";
    case LessLexer.FREQ:
      return "FREQ";
    case LessLexer.HEXCHAR:
      return "HEXCHAR";
    case LessLexer.NONASCII:
      return "NONASCII";
    case LessLexer.UNICODE:
      return "UNICODE";
    case LessLexer.ESCAPE:
      return "ESCAPE";
    case LessLexer.NMSTART:
      return "NMSTART";
    case LessLexer.NMCHAR:
      return "NMCHAR";
    case LessLexer.NAME:
      return "NAME";
    case LessLexer.URL:
      return "URL";
    case LessLexer.A:
      return "A";
    case LessLexer.B:
      return "B";
    case LessLexer.C:
      return "C";
    case LessLexer.D:
      return "D";
    case LessLexer.E:
      return "E";
    case LessLexer.F:
      return "F";
    case LessLexer.G:
      return "G";
    case LessLexer.H:
      return "H";
    case LessLexer.I:
      return "I";
    case LessLexer.J:
      return "J";
    case LessLexer.K:
      return "K";
    case LessLexer.L:
      return "L";
    case LessLexer.M:
      return "M";
    case LessLexer.N:
      return "N";
    case LessLexer.O:
      return "O";
    case LessLexer.P:
      return "P";
    case LessLexer.Q:
      return "Q";
    case LessLexer.R:
      return "R";
    case LessLexer.S:
      return "S";
    case LessLexer.T:
      return "T";
    case LessLexer.U:
      return "U";
    case LessLexer.V:
      return "V";
    case LessLexer.W:
      return "W";
    case LessLexer.X:
      return "X";
    case LessLexer.Y:
      return "Y";
    case LessLexer.Z:
      return "Z";
    case LessLexer.COMMENT:
      return "COMMENT";
    case LessLexer.NL:
      return "NL";
    case LessLexer.COMMENT_LITTLE:
      return "COMMENT_LITTLE";
    case LessLexer.CDO:
      return "CDO";
    case LessLexer.CDC:
      return "CDC";
    case LessLexer.INVALID:
      return "INVALID";
    case LessLexer.ESCAPED_SIMBOL:
      return "ESCAPED_SIMBOL";
    case LessLexer.HASH_FRAGMENT:
      return "HASH_FRAGMENT";
    case LessLexer.WS:
      return "WS";
//    case LessLexer.HASH_COMBINATOR:
//      return "HASH_COMBINATOR";
    case LessLexer.REPEATER:
      return "REPEATER";
    case LessLexer.PURE_NUMBER:
      return "PURE_NUMBER";
//    case LessLexer.NUMBER_UNKNOWN_DIMENSION:
//      return "NUMBER_UNKNOWN_DIMENSION";
//    case LessLexer.ODD:
//      return "ODD";
//    case LessLexer.EVEN:
//      return "EVEN";
    case LessLexer.NEW_LINE:
      return "NEW_LINE";
    default:
      return "Unknown: " + type;
    }
  }

  public static void print(HiddenTokenAwareTree ast) {
    print(ast, 0);
  }

  private static void print(HiddenTokenAwareTree tree, int level) {
    indent(level);
    printSingleNode(tree);

    // print all children
    if (tree.getChildren() != null)
      for (Object ie : tree.getChildren()) {
        print((HiddenTokenAwareTree) ie, level + 1);
      }
  }

  public static void indent(int level) {
    for (int i = 0; i < level; i++)
      System.out.print("--");
  }

  public static void printSingleNode(HiddenTokenAwareTree tree) {
    String base = " " + tree.getType() + " " + tree.getText();
    if (printIndexes) {
      String optionalsuffix = " " + tree.getTokenStartIndex() + "-" + tree.getTokenStopIndex() + " " + (tree.getToken() == null ? "" : tree.getToken().getTokenIndex());
      base = base + optionalsuffix;
    }
    if (printComments) {
      String optionalsuffix = " PC:" + tree.getPreceding().size() + " FC:" + tree.getFollowing().size();
      base = base + optionalsuffix;
    }
    System.out.println(base);
  }

}
