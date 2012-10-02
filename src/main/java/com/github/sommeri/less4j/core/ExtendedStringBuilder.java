package com.github.sommeri.less4j.core;

import java.util.HashSet;
import java.util.Set;

import com.github.sommeri.less4j.platform.Constants;

public class ExtendedStringBuilder {
  private static final String INDENTATION = "  ";
  private static final char SPACE = ' ';
  private static final Set<Character> SEPARATORS = new HashSet<Character>();
  private StringBuilder builder = new StringBuilder();
  private int indentationLevel;
  private boolean onNewLine = true;

  static {
    SEPARATORS.add(' ');
    SEPARATORS.add(';');
    SEPARATORS.add('(');
    //SEPARATORS.add(')');
  }

  public ExtendedStringBuilder(String string) {
    builder = new StringBuilder(string);
  }

  public ExtendedStringBuilder append(boolean arg0) {
    handleIndentation();
    builder.append(arg0);
    return this;
  }

  public ExtendedStringBuilder append(char c) {
    handleIndentation();
    builder.append(c);
    return this;
  }

  public ExtendedStringBuilder append(char[] str, int offset, int len) {
    handleIndentation();
    builder.append(str, offset, len);
    return this;
  }

  public ExtendedStringBuilder append(char[] str) {
    handleIndentation();
    builder.append(str);
    return this;
  }

  public ExtendedStringBuilder append(CharSequence s, int start, int end) {
    handleIndentation();
    builder.append(s, start, end);
    return this;
  }

  public ExtendedStringBuilder append(CharSequence s) {
    handleIndentation();
    builder.append(s);
    return this;
  }

  public ExtendedStringBuilder append(double d) {
    handleIndentation();
    builder.append(d);
    return this;
  }

  public ExtendedStringBuilder append(float f) {
    handleIndentation();
    builder.append(f);
    return this;
  }

  public ExtendedStringBuilder append(int i) {
    handleIndentation();
    builder.append(i);
    return this;
  }

  public ExtendedStringBuilder append(long lng) {
    handleIndentation();
    builder.append(lng);
    return this;
  }

  public ExtendedStringBuilder append(Object obj) {
    handleIndentation();
    builder.append(obj);
    return this;
  }

  public ExtendedStringBuilder append(String str) {
    handleIndentation();
    builder.append(str);
    return this;
  }

  public ExtendedStringBuilder append(StringBuffer sb) {
    handleIndentation();
    builder.append(sb);
    return this;
  }

  public ExtendedStringBuilder appendIgnoreNull(CharSequence s) {
    if (s != null) {
      handleIndentation();
      builder.append(s);
    }

    return this;
  }

  public String toString() {
    return builder.toString();
  }

  public void ensureNewLine() {
    if (!onNewLine)
      newLine();
  }

  public void newLine() {
    builder.append(Constants.NEW_LINE);
    onNewLine = true;
  }

  public void increaseIndentationLevel() {
    indentationLevel++;
  }

  public void decreaseIndentationLevel() {
    indentationLevel--;
    if (indentationLevel < 0)
      indentationLevel = 0;
  }

  private void handleIndentation() {
    if (onNewLine()) {
      onNewLine = false;
      for (int i = 0; i < indentationLevel; i++) {
        builder.append(INDENTATION);
      }
    }
  }

  private boolean onNewLine() {
    return onNewLine;
  }

  public ExtendedStringBuilder ensureSeparator() {
    if (onNewLine() || endsWithSeparator())
      return this;

    builder.append(SPACE);
    return this;
  }

  public boolean endsWithSeparator() {
    int length = builder.length() - 1;
    if (length < 0)
      return false;

    return SEPARATORS.contains(builder.charAt(length));
  }

}
