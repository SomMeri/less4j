package com.github.sommeri.less4j.core.compiler.expressions.strings;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public abstract class AbstractStringReplacer <ARG> {

  public AbstractStringReplacer() {
    super();
  }

  public String replaceIn(String originalValue, ARG replacementArgument, HiddenTokenAwareTree technicalUnderlying) {
    String replacedValue = replacementRound(originalValue, replacementArgument, technicalUnderlying);
    while (shouldIterate() && !equals(replacedValue, originalValue)) {
      originalValue = replacedValue;
      replacedValue = replacementRound(replacedValue, replacementArgument, technicalUnderlying);
    }
    return replacedValue;
  }

  protected abstract boolean shouldIterate();

  private boolean equals(String replacedValue, String originalValue) {
    if (replacedValue==null)
      return originalValue==null;

    return replacedValue.equals(originalValue);
  }

  private String replacementRound(String originalValue, ARG replacementArgument, HiddenTokenAwareTree technicalUnderlying) {
    StringBuilder result = new StringBuilder();
    List<MatchRange> matches = findMatches(originalValue);
    int lastEnd = 0;
    for (MatchRange matchRange : matches) {
      // add everything from the last end to match from
      result.append(originalValue.substring(lastEnd, matchRange.getFrom()));
      // replace match by value
      result.append(replacementValue(replacementArgument, technicalUnderlying, matchRange));
      // update last end
      lastEnd = matchRange.getTo();
    }
    // add everything from the last end to end of string
    if (lastEnd<originalValue.length())
      result.append(originalValue.substring(lastEnd));
    
    return result.toString();
  }

  private List<MatchRange> findMatches(String originalValue) {
    List<MatchRange> result = new ArrayList<MatchRange>();
    Matcher matcher = getPattern().matcher(originalValue);
    while (matcher.find()) {
      result.add(createMatchRange(matcher));
    }
  
    return result;
  }

  private MatchRange createMatchRange(Matcher matcher) {
    String group = matcher.group();
    return new MatchRange(matcher.start(), matcher.end(), extractMatchName(group), group);
  }

  protected abstract String extractMatchName(String group);

  protected abstract Pattern getPattern();

  protected abstract String replacementValue(ARG replacementArgument, HiddenTokenAwareTree technicalUnderlying, MatchRange matchRange);
  
}