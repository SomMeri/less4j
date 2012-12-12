package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.InStringCssPrinter;

public class StringInterpolator {

  private final static Pattern STR_INTERPOLATION = Pattern.compile("@\\{([^\\{\\}@])*\\}");

  public String replaceInterpolatedVariables(String originalValue, ExpressionEvaluator expressionEvaluator, HiddenTokenAwareTree technicalUnderlying) {
    StringBuilder result = new StringBuilder();
    List<MatchRange> matches = findMatches(originalValue);
    int lastEnd = 0;
    for (MatchRange matchRange : matches) {
      // add everything from the last end to match from
      result.append(originalValue.substring(lastEnd, matchRange.getFrom()));
      // replace match by value
      result.append(evaluate(expressionEvaluator, technicalUnderlying, matchRange));
      // update last end
      lastEnd = matchRange.getTo();
    }
    // add everything from the last end to end of string
    if (lastEnd<originalValue.length())
      result.append(originalValue.substring(lastEnd));
    
    return result.toString();
  }

  private String evaluate(ExpressionEvaluator expressionEvaluator, HiddenTokenAwareTree technicalUnderlying, MatchRange matchRange) {
    Expression value = expressionEvaluator.evaluateIfPresent(new Variable(technicalUnderlying, matchRange.getVariableName()));
    if (value!=null && (value instanceof CssString)) {
      CssString string = (CssString) value;
      return replaceInterpolatedVariables(string.getValue(), expressionEvaluator, technicalUnderlying);
    } else if (value==null) {
      return matchRange.getFullMatch();
    } else {
      InStringCssPrinter builder = new InStringCssPrinter();
      builder.append(value);
      String replacement = builder.toString();
      return replacement;
    }
  }

  private List<MatchRange> findMatches(String originalValue) {
    List<MatchRange> result = new ArrayList<MatchRange>();
    Matcher matcher = STR_INTERPOLATION.matcher(originalValue);
    while (matcher.find()) {
      result.add(createMatchRange(matcher));
    }

    return result;
  }

  private MatchRange createMatchRange(Matcher matcher) {
    String group = matcher.group();
    return new MatchRange(matcher.start(), matcher.end(), "@"+group.substring(2, group.length()-1), group);
  }

}

class MatchRange {
  
  private final int from;
  private final int to;
  private final String variableName;
  private final String fullMatch;
  
  public MatchRange(int from, int to, String variableName, String fullMatch) {
    super();
    this.from = from;
    this.to = to;
    this.variableName = variableName;
    this.fullMatch = fullMatch;
  }

  public int getFrom() {
    return from;
  }

  public int getTo() {
    return to;
  }

  public String getVariableName() {
    return variableName;
  }

  public String getFullMatch() {
    return fullMatch;
  }

}
