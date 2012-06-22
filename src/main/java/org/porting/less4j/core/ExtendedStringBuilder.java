package org.porting.less4j.core;

public class ExtendedStringBuilder {
  private StringBuilder builder = new StringBuilder();

  public ExtendedStringBuilder(String string) {
    builder = new StringBuilder(string);
  }

  public ExtendedStringBuilder append(boolean arg0) {
    builder.append(arg0);
    return this;
  }

  public ExtendedStringBuilder append(char c) {
    builder.append(c);
    return this;
  }

  public ExtendedStringBuilder append(char[] str, int offset, int len) {
    builder.append(str, offset, len);
    return this;
  }

  public ExtendedStringBuilder append(char[] str) {
    builder.append(str);
    return this;
  }

  public ExtendedStringBuilder append(CharSequence s, int start, int end) {
    builder.append(s, start, end);
    return this;
  }

  public ExtendedStringBuilder append(CharSequence s) {
    builder.append(s);
    return this;
  }

  public ExtendedStringBuilder append(double d) {
    builder.append(d);
    return this;
  }

  public ExtendedStringBuilder append(float f) {
    builder.append(f);
    return this;
  }

  public ExtendedStringBuilder append(int i) {
    builder.append(i);
    return this;
  }

  public ExtendedStringBuilder append(long lng) {
    builder.append(lng);
    return this;
  }

  public ExtendedStringBuilder append(Object obj) {
    builder.append(obj);
    return this;
  }

  public ExtendedStringBuilder append(String str) {
    builder.append(str);
    return this;
  }

  public ExtendedStringBuilder append(StringBuffer sb) {
    builder.append(sb);
    return this;
  }

  public ExtendedStringBuilder appendIgnoreNull(CharSequence s) {
    if (s != null)
      builder.append(s);
    
    return this;
  }

  public String toString() {
    return builder.toString();
  }

  
}
