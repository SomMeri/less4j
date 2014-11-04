package com.github.sommeri.less4j.core.output;

import java.io.IOException;
import java.util.Collection;

import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.LessSource.CannotReadFile;
import com.github.sommeri.less4j.LessSource.FileNotFound;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.problems.BugHappened;
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
  private final LessCompiler.SourceMapConfiguration configuration;
  private final Collection<LessSource> additionalSourceFiles;

  private LessSource cssDestination;

  public SourceMapBuilder(ExtendedStringBuilder cssBuilder, LessSource cssDestination, Collection<LessSource> additionalSourceFiles, LessCompiler.SourceMapConfiguration configuration) {
    this.cssBuilder = cssBuilder;
    this.cssDestination = cssDestination;
    this.configuration = configuration;
    this.additionalSourceFiles = additionalSourceFiles;
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

  private void createMapping(String mappedSymbol, HiddenTokenAwareTree sourceToken, FilePosition outputStartPosition, FilePosition outputEndPosition) {
    FilePosition sourceStartPosition = toFilePosition(sourceToken);
    String sourceName = toSourceName(sourceToken);
    String sourceContent = toSourceContent(sourceToken, sourceName);

    generator.addMapping(sourceName, sourceContent, mappedSymbol, sourceStartPosition, outputStartPosition, outputEndPosition);
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
    FilePosition result = new FilePosition(underlyingStructure.getLine() - 1, underlyingStructure.getCharPositionInLine());
    return result;
  }

  private String toSourceName(HiddenTokenAwareTree underlyingStructure) {
    LessSource source = underlyingStructure.getSource();
    return toSourceName(source);
  }

  private String toSourceName(LessSource source) {
    if (configuration.isRelativizePaths()) {
      return URIUtils.relativizeSourceURIs(cssDestination, source);
    } else {
      return source.getURI() == null ? null : source.getURI().toString();
    }
  }

  private String toSourceContent(HiddenTokenAwareTree underlyingStructure, String sourceName) {
    LessSource source = underlyingStructure.getSource();
    return toSourceContent(underlyingStructure, sourceName, source);
  }

  private String toSourceContent(HiddenTokenAwareTree underlyingStructure, String sourceName, LessSource source) {
    if (configuration.isIncludeSourcesContent() || sourceName==null) { 
      try {
        return source.getContent();
      } catch (FileNotFound e) {
        throw new BugHappened("How did we compiled something we did not read?", underlyingStructure);
      } catch (CannotReadFile e) {
        throw new BugHappened("How did we compiled something we did not read?", underlyingStructure);
      }
    } else {
      return null;
    }
  }

  public String toSourceMap() {
    for (LessSource source : additionalSourceFiles) {
      String sourceName = toSourceName(source);
      String sourceContent = toSourceContent(null, sourceName, source);
      generator.addSourceFile(sourceName, sourceContent);
    }
    // map file is assumed to have the same location as generated css 
    String name = "";
    if (cssDestination != null && cssDestination.getName() != null) {
      name = cssDestination.getName();
    }
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
