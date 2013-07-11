package com.github.sommeri.less4j.core.output;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class NullSourceMapBuilder {

  private final ExtendedStringBuilder cssBuilder;

  public NullSourceMapBuilder(ExtendedStringBuilder cssBuilder) {
    this.cssBuilder = cssBuilder;
  }

  public void appendAsSymbol(String str, HiddenTokenAwareTree underlyingStructure) {
    cssBuilder.append(str);
  }

  public ExtendedStringBuilder getCssBuilder() {
    return cssBuilder;
  }

  public String toSourceMap() {
    return null;
  }

  public void append(SourceMapBuilder other) {
    cssBuilder.appendAsIs(other.getCssBuilder().toString());
  }

}
