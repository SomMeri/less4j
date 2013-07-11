package com.github.sommeri.less4j.core.output;

import java.io.IOException;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.URIUtils;
import com.github.sommeri.sourcemap.FilePosition;
import com.github.sommeri.sourcemap.SourceMapFormat;
import com.github.sommeri.sourcemap.SourceMapGenerator;
import com.github.sommeri.sourcemap.SourceMapGeneratorFactory;

//FIXME: source map: Line 6: An optional list of source content, useful when the “source” can’t be hosted. - use it in case of input string source
public class SourceMapBuilder implements ISourceMapBuilder {

  private final ExtendedStringBuilder cssBuilder;
  private final SourceMapGenerator generator;

  private LessSource lessSource;
  private LessSource cssDestination;

  public SourceMapBuilder(ExtendedStringBuilder cssBuilder, LessSource lessSource, LessSource cssDestination) {
    this.cssBuilder = cssBuilder;
    this.lessSource = lessSource;
    this.cssDestination = cssDestination;
    generator = SourceMapGeneratorFactory.getInstance(SourceMapFormat.V3);
  }

  /* (non-Javadoc)
   * @see com.github.sommeri.less4j.core.output.IISourceMapBuilder#appendAsSymbol(java.lang.String, com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree)
   */
  @Override
  public void appendAsSymbol(String str, HiddenTokenAwareTree underlyingStructure) {
    FilePosition outputStartPosition = new FilePosition(cssBuilder.getLine(), cssBuilder.getColumn());
    cssBuilder.append(str);
    FilePosition outputEndPosition = new FilePosition(cssBuilder.getLine(), cssBuilder.getColumn());
    
    FilePosition sourceStartPosition = toFilePosition(underlyingStructure);
    String sourceName = toSourceName(underlyingStructure);
    generator.addMapping(sourceName, str, sourceStartPosition, outputStartPosition, outputEndPosition);
  }
  
  public void append(ISourceMapBuilder other) {
    FilePosition offset = new FilePosition(cssBuilder.getLine(), cssBuilder.getColumn());
    cssBuilder.appendAsIs(other.getCssBuilder().toString());
    SourceMapGenerator otherGenerator = other.getInternalGenerator();
    generator.offsetAndAppend(otherGenerator, offset);
  }

  public ExtendedStringBuilder getCssBuilder() {
    return cssBuilder;
  }
  
  private FilePosition toFilePosition(HiddenTokenAwareTree underlyingStructure) {
    FilePosition result = new FilePosition(underlyingStructure.getLine()-1, underlyingStructure.getCharPositionInLine()-1);
    return result;
  }

  private String toSourceName(HiddenTokenAwareTree underlyingStructure) {
    return URIUtils.relativizeSourceURIs(cssDestination, underlyingStructure.getSource());
  }
  
  @Override
  public String toSourceMap() {
    String name = URIUtils.relativizeSourceURIs(lessSource, cssDestination);
    try {
      StringBuilder sb = new StringBuilder();
      generator.appendTo(sb, name);
      return sb.toString();
    } catch (IOException e) {
      throw new IllegalStateException("Impossible to happen exception.", e);
    }
  }

  @Override
  public SourceMapGenerator getInternalGenerator() {
    return generator;
  }

}
