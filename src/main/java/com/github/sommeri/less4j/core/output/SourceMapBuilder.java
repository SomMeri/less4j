package com.github.sommeri.less4j.core.output;

import java.io.IOException;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.URIUtils;
import com.github.sommeri.sourcemap.FilePosition;
import com.github.sommeri.sourcemap.SourceMapFormat;
import com.github.sommeri.sourcemap.SourceMapGenerator;
import com.github.sommeri.sourcemap.SourceMapGeneratorFactory;

/*
   Unused source map v3 features: source content and source root.
 */
public class SourceMapBuilder {

  private final ExtendedStringBuilder cssBuilder;
  private final SourceMapGenerator generator;

  private LessSource cssDestination;

  public SourceMapBuilder(ExtendedStringBuilder cssBuilder, LessSource cssDestination) {
    this.cssBuilder = cssBuilder;
    this.cssDestination = cssDestination;
    generator = SourceMapGeneratorFactory.getInstance(SourceMapFormat.V3);
  }

  public SourceMapBuilder append(String str, HiddenTokenAwareTree sourceToken) {
    //indentation must be handled before measuring symbol start position
    FilePosition outputStartPosition = beforeSymbolPosition();
    cssBuilder.append(str);
    FilePosition outputEndPosition = afterSymbolPosition();
    
    createMapping(str, sourceToken, outputStartPosition, outputEndPosition);
    return this;
  }

  public void appendIgnoreNull(String str, HiddenTokenAwareTree sourceToken) {
    FilePosition outputStartPosition = beforeSymbolPosition();
    cssBuilder.append(str);
    FilePosition outputEndPosition = afterSymbolPosition();
    
    createMapping(str, sourceToken, outputStartPosition, outputEndPosition);
  }

  private void createMapping(String str, HiddenTokenAwareTree sourceToken, FilePosition outputStartPosition, FilePosition outputEndPosition) {
    FilePosition sourceStartPosition = toFilePosition(sourceToken);
    String sourceName = toSourceName(sourceToken);
    generator.addMapping(sourceName, str, sourceStartPosition, outputStartPosition, outputEndPosition);
  }

  private FilePosition beforeSymbolPosition() {
    cssBuilder.handleIndentation();
    FilePosition outputStartPosition = afterSymbolPosition();
    return outputStartPosition;
  }

  private FilePosition afterSymbolPosition() {
    return currentPosition();
  }

  private FilePosition currentPosition() {
    return new FilePosition(cssBuilder.getLine(), cssBuilder.getColumn());
  }
  
  public void append(SourceMapBuilder other) {
    FilePosition offset = afterSymbolPosition();
    cssBuilder.appendAsIs(other.cssBuilder.toString());
    SourceMapGenerator otherGenerator = other.generator;
    generator.offsetAndAppend(otherGenerator, offset);
  }

  private FilePosition toFilePosition(HiddenTokenAwareTree underlyingStructure) {
    FilePosition result = new FilePosition(underlyingStructure.getLine()-1, underlyingStructure.getCharPositionInLine());
    return result;
  }

  private String toSourceName(HiddenTokenAwareTree underlyingStructure) {
    return URIUtils.relativizeSourceURIs(cssDestination, underlyingStructure.getSource());
  }
  
  public String toSourceMap() {
    // map file is assumed to have the same location as generated css 
    String name = cssDestination.getName()==null? "" : cssDestination.getName();
    try {
      StringBuilder sb = new StringBuilder();
      generator.appendTo(sb, name);
      return sb.toString();
    } catch (IOException e) {
      throw new IllegalStateException("Impossible to happen exception.", e);
    }
  }

  protected ExtendedStringBuilder getCssBuilder() {
    return cssBuilder;
  }

  public void ensureSeparator() {
    cssBuilder.ensureSeparator();
  }

}
