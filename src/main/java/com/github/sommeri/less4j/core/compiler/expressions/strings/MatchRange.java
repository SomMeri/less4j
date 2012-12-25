package com.github.sommeri.less4j.core.compiler.expressions.strings;

class MatchRange {
  
  private final int from;
  private final int to;
  private final String name;
  private final String fullMatch;
  
  public MatchRange(int from, int to, String name, String fullMatch) {
    super();
    this.from = from;
    this.to = to;
    this.name = name;
    this.fullMatch = fullMatch;
  }

  public int getFrom() {
    return from;
  }

  public int getTo() {
    return to;
  }

  public String getName() {
    return name;
  }

  public String getFullMatch() {
    return fullMatch;
  }

}