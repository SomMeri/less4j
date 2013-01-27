package com.github.sommeri.less4j.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
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
    return toTypeName(node.getType());
  }

  public static String toTypeNames(ASTCssNodeType... types) {
    String result = "";
    for (ASTCssNodeType type : types) {
      if (!result.isEmpty())
        result +=" or ";
      
      result +=toTypeName(type);
    }
    
    return result;
  }
  
  public static String toTypeName(ASTCssNodeType type) {
    switch (type) {
    case DECLARATION:
      return "declaration";

    case KEYFRAMES_BODY:
      return "@keyframes";

    case STYLE_SHEET:
      return "top level style sheet";

    case INDIRECT_VARIABLE:
      return "indirect variable";

    case VARIABLE:
      return "variable";

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

    default:
      //TODO (low priority) replace by something safer
      return type.name();
    }
  }

  public static String toLocation(ASTCssNode node) {
    return node.getSourceLine() + ":" + node.getCharPositionInSourceLine();
  }

}
