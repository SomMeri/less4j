package com.github.sommeri.less4j.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import javax.xml.bind.DatatypeConverter;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.parser.LessParser;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class PrintUtils {

  private static final DecimalFormat FORMATTER = createFormatter();
  private static final DecimalFormat FORMATTER_TWO_DECIMAL = createFormatter2();
  
  private static DecimalFormat createFormatter() {
    DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
    symbols.setDecimalSeparator('.');
    return new DecimalFormat("#.##################", symbols);
  }

  private static DecimalFormat createFormatter2() {
    DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
    symbols.setDecimalSeparator('.');
    return new DecimalFormat("#.####", symbols);
  }

  public static String formatNumber(Long value) {
    return formatNumber(value.doubleValue());
  }

  public static String formatNumber(Double value) {
    if (value.isNaN())
      return "NaN";

    return FORMATTER.format(value);
  }
  
  public static String formatNumberTwoDecimal(Long value) {
    return formatNumberTwoDecimal(value.doubleValue());
  }
  
  public static String formatNumberTwoDecimal(Double value) {
    if (value.isNaN())
      return "NaN";

    return FORMATTER_TWO_DECIMAL.format(value);
  }
  
  public static String formatNumber(Number value) {
    return FORMATTER.format(value);
  }

  public static String toName(int tokenType) {
    if (tokenType == -1)
      return "EOF";

    if (tokenType >= LessParser.tokenNames.length)
      return "Unknown: " + tokenType;

    return LessParser.tokenNames[tokenType];
  }

  public static String toUtf8ExceptURL(String value) {
    String encode = toUtf8(value);
    encode = encode.replaceAll("\\+", "%20").replaceAll("%2C", ",").replaceAll("%2F", "/").replaceAll("%3F", "?");
    encode = encode.replaceAll("%40", "@").replaceAll("%26", "&").replaceAll("%2B", "+");
    encode = encode.replaceAll("%27", "'").replaceAll("%7E", "~").replaceAll("%21", "!");
    encode = encode.replace("%24", "$"); //replaceAll crash on this
    return encode;
  }

  /**
   * Port of javascript encodeURIComponent.
   * 
   * Converts into utf-8 except following characters * - _ . ! ~ * ' ( )
   */
  public static String toUtf8AsUri(String value) { // 
    String encode = toUtf8(value);
    encode = encode.replaceAll("\\+", "%20").replaceAll("%2D", "_").replaceAll("%2E", ".");
    encode = encode.replaceAll("%21", "!").replaceAll("%7E", "~").replaceAll("%2A", "*");
    encode = encode.replaceAll("%27", "'").replaceAll("%28", "(").replaceAll("%29", ")");
    return encode;
  }

  public static String toUtf8ExceptGrrr(String value) {
    String encode = toUtf8(value);
    encode = encode.replaceAll("\\+", "%20");
    encode = encode.replaceAll("%28", "(").replaceAll("%29", ")");
    encode = encode.replaceAll("%27", "'").replaceAll("%7E", "~").replaceAll("%21", "!");
    return encode;
  }

  public static String urlEncode(String toEncode, String encodingCharset, ProblemsHandler problemsHandler, ASTCssNode nodeForErrorReport) {
    if (toEncode==null || encodingCharset==null)
      return null;
    try {
      return URLEncoder.encode(toEncode, encodingCharset);
    } catch (UnsupportedEncodingException uex) {
      problemsHandler.errUnknownEncodingCharsetSourceMap(nodeForErrorReport, encodingCharset);
      return null;
    }
  }

  public static String base64Encode(String toEncode, String encodingCharset, ProblemsHandler problemsHandler, ASTCssNode nodeForErrorReport) {
    try {
      return DatatypeConverter.printBase64Binary(toEncode.getBytes(encodingCharset));
//      return toEncode;
    } catch (UnsupportedEncodingException uex) {
      problemsHandler.errUnknownEncodingCharsetSourceMap(nodeForErrorReport, encodingCharset);
      return null;
    }
  }

  public static String toUtf8(String value) {
    try {
      return URLEncoder.encode(value, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException(e);
    }
  }

  public static String toTypeName(ASTCssNode node) {
    return toTypeName(node.getType());
  }

  public static String toTypeNames(ASTCssNodeType... types) {
    String result = "";
    for (ASTCssNodeType type : types) {
      if (!result.isEmpty())
        result += " or ";

      result += toTypeName(type);
    }

    return result;
  }

  public static String toTypeName(ASTCssNodeType type) {
    switch (type) {
    case DECLARATION:
      return "declaration";

    case KEYFRAMES:
      return "@keyframes";

    case DOCUMENT:
      return "@document";

    case STYLE_SHEET:
      return "top level style sheet";

    case INDIRECT_VARIABLE:
      return "indirect variable";

    case VARIABLE:
      return "expression";

    case NUMBER:
      return "number";

    case NAMED_EXPRESSION:
      return "identifier";

    case IDENTIFIER_EXPRESSION:
      return "identifier";

    case FUNCTION:
      return "function";

    case FAULTY_EXPRESSION:
      return "faulty expression";

    case ESCAPED_VALUE:
      return "escaped value";

    case EMPTY_EXPRESSION:
      return "empty expression";

    case COLOR_EXPRESSION:
      return "color";

    case STRING_EXPRESSION:
      return "string";

    case RULE_SET:
      return "rule set";

    case PAGE:
      return "@page";

    case PAGE_MARGIN_BOX:
      return "page margin box";

    default:
      //FIXME (hign priority) replace by something safer - ASAP
      return type.name();
    }
  }

  public static String toLocation(ASTCssNode node) {
    return node.getSourceLine() + ":" + node.getSourceColumn();
  }

  public static String base64Encode(byte[] data) {
    return DatatypeConverter.printBase64Binary(data);
  }

  public static String toString(String[] strings) {
    StringBuilder result = new StringBuilder();
    boolean first = true;
    for (String string : strings) {
      if (!first)
        result.append(", ");
      result.append(string);
      first = false;
    }
    return result.toString();
  }

}
