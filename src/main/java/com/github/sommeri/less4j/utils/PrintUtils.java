package com.github.sommeri.less4j.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.parser.LessParser;

public class PrintUtils {
  
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

  public static String toUtf8ExceptGrrr(String value) {
    String encode = toUtf8(value);
    encode = encode.replaceAll("\\+", "%20");
    encode = encode.replaceAll("%28", "(").replaceAll("%29", ")");
    encode = encode.replaceAll("%27", "'").replaceAll("%7E", "~").replaceAll("%21", "!");
    return encode;
  }

  public static String toUtf8(String value) {
    try {
      return URLEncoder.encode(value, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException(e);
    }
  }

  public static String toTypeName(ASTCssNode node) {
    switch (node.getType()) {
    case DECLARATION:
      return "declaration";

    case KEYFRAMES_BODY:
      return "@keyframes";

    case STYLE_SHEET:
      return "top level style sheet";

    default:
      //TODO (low priority) replace by something safer
      return node.getType().name();
    }
  }

  public static String toLocation(ASTCssNode node) {
    return node.getSourceLine() +":"+node.getCharPositionInSourceLine();
  }

}
