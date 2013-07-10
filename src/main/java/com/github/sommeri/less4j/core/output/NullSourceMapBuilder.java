package com.github.sommeri.less4j.core.output;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.sourcemap.SourceMapFormat;
import com.github.sommeri.sourcemap.SourceMapGenerator;
import com.github.sommeri.sourcemap.SourceMapGeneratorFactory;

public class NullSourceMapBuilder implements ISourceMapBuilder {

  private final ExtendedStringBuilder cssBuilder;

  public NullSourceMapBuilder(ExtendedStringBuilder cssBuilder) {
    this.cssBuilder = cssBuilder;
  }

  @Override
  public void appendAsSymbol(String str, HiddenTokenAwareTree underlyingStructure) {
    cssBuilder.append(str);
  }

  public ExtendedStringBuilder getCssBuilder() {
    return cssBuilder;
  }

  @Override
  public String toSourceMap() {
    return null;
  }

  @Override
  public SourceMapGenerator getInternalGenerator() {
    return SourceMapGeneratorFactory.getInstance(SourceMapFormat.V3);
  }

  @Override
  public void append(ISourceMapBuilder other) {
    cssBuilder.appendAsIs(other.getCssBuilder().toString());
  }

}
