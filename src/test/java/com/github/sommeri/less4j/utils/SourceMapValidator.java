package com.github.sommeri.less4j.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.sourcemap.FilePosition;
import com.github.sommeri.sourcemap.SourceMapConsumerV3;
import com.github.sommeri.sourcemap.SourceMapConsumerV3.EntryVisitor;
import com.github.sommeri.sourcemap.SourceMapParseException;

public class SourceMapValidator {

  private static final String INTERPOLATED_SYMBOLS_PROPERTY = "interpolatedNames";
  private static final String SYMBOLS_PROPERTY = "names";
  private static final String SOURCES_PROPERTY = "sources";

  private Map<String, MappedFile> mappedFiles = new HashMap<String, MappedFile>();

  public SourceMapValidator() {
  }

  //FIXME: source map - get rid of root param -- both add and read it from map
  public void validateSourceMap(File lessFile, CompilationResult compilationResult, File root, File mapdataFile) {
    SourceMapConsumerV3 sourceMap = parseGeneratedMap(compilationResult);

    Mapdata mapdata = checkAgainstMapdataFile(sourceMap, mapdataFile);
    loadMappedSourceFiles(sourceMap.getOriginalSources(), root);

    MappingEntryValidation mappingEntryValidation = new MappingEntryValidation(mapdata);
    sourceMap.visitMappings(mappingEntryValidation);
  }

  private SourceMapConsumerV3 parseGeneratedMap(CompilationResult compilationResult) {
    try {
      SourceMapConsumerV3 sourceMap = new SourceMapConsumerV3();
      sourceMap.parse(compilationResult.getSourceMap());
      return sourceMap;
    } catch (SourceMapParseException e) {
      throw new RuntimeException(e);
    }
  }

  private Mapdata checkAgainstMapdataFile(SourceMapConsumerV3 sourceMap, File mapdataFile) {
    Mapdata expectedMapdata = loadMapdata(mapdataFile);

    // validate mapdata file
    if (expectedMapdata.hasSources()) { 
      CustomAssertions.assertEqualsAsSets(expectedMapdata.getSources(), sourceMap.getOriginalSources());
    }
    if (expectedMapdata.hasSymbols()) {
      CustomAssertions.assertEqualsAsSets(expectedMapdata.getSymbols(), allSymbols(sourceMap));
    }
    return expectedMapdata;
  }

  private Collection<String> allSymbols(SourceMapConsumerV3 sourceMap) {
    SymbolsCollector symbolsCollector = new SymbolsCollector();
    sourceMap.visitMappings(symbolsCollector);
    return symbolsCollector.getSymbols();
  }

  private Mapdata loadMapdata(File mapdataFile) {
    // mapdata file not available - it is assumed to be empty
    if (mapdataFile == null || !mapdataFile.exists())
      return new Mapdata();

    try {
      JSONTokener tokener = new JSONTokener(new FileReader(mapdataFile));
      JSONObject mapdata = new JSONObject(tokener);

      List<String> expectedSources = JSONUtils.getStringList(mapdata, SOURCES_PROPERTY);
      List<String> expectedSymbols = JSONUtils.getStringList(mapdata, SYMBOLS_PROPERTY);
      List<String> interpolatedSymbols = JSONUtils.getStringList(mapdata, INTERPOLATED_SYMBOLS_PROPERTY);

      return new Mapdata(expectedSources, expectedSymbols, interpolatedSymbols);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  private void loadMappedSourceFiles(Collection<String> originalSources, File root) {
    for (String name : originalSources) {
      try {
        File file = new File(root, URLDecoder.decode(name, "utf-8"));
        String content = IOUtils.toString(new FileReader(file));
        String[] lines = content.split("\r?\n|\r");
        MappedFile mapped = new MappedFile(lines);
        mappedFiles.put(name, mapped);
      } catch (Throwable th) {
        fail("Could not read source file " + name);
      }
    }
  }

  private final class SymbolsCollector implements EntryVisitor {

    private final Set<String> symbols = new HashSet<String>();

    private SymbolsCollector() {
    }

    @Override
    public void visit(String sourceName, String symbolName, FilePosition sourceStartPosition, FilePosition startPosition, FilePosition endPosition) {
      if (symbolName != null)
        symbols.add(symbolName);
    }

    public Set<String> getSymbols() {
      return symbols;
    }

  }

  class MappingEntryValidation implements EntryVisitor {

    private Mapdata mapdata;

    public MappingEntryValidation(Mapdata mapdata) {
      this.mapdata = mapdata;
    }

    @Override
    public void visit(String sourceName, String symbolName, FilePosition sourceStartPosition, FilePosition startPosition, FilePosition endPosition) {
      MappedFile mappedFile = mappedFiles.get(sourceName);
      if (symbolName != null && !mapdata.isInterpolated(symbolName)) {
        String sourceSnippet = mappedFile.getSnippet(sourceStartPosition, symbolName.length());
        
        assertNotNull(sourceName + ": css symbol " + symbolName + " " + startPosition + " is mapped non-existent source position  " + sourceStartPosition, sourceSnippet);
        assertEquals(sourceName + ": css symbol " + symbolName + " " + startPosition + " is mapped to less " + sourceSnippet + " " + sourceStartPosition, symbolName, sourceSnippet);
      }
    }

  }
}

class MappedFile {

  private final String[] lines;

  public MappedFile(String[] lines) {
    this.lines = lines;
  }

  public String getSnippet(FilePosition start, int length) {
    if (start.getLine() >= lines.length)
      return null;

    String line = lines[start.getLine()];

    int startColumn = start.getColumn();
    int end = startColumn + length;
    if (end >= line.length())
      return null;

    String substring = line.substring(startColumn, end);
    return substring;
  }

}

class Mapdata {

  private List<String> sources = null;
  private List<String> symbols = null;
  private List<String> interpolatedSymbols = null;

  public Mapdata(List<String> sources, List<String> symbols, List<String> interpolatedSymbols) {
    this.sources = sources;
    this.symbols = symbols;
    this.interpolatedSymbols = interpolatedSymbols;
  }

  public Mapdata() {
  }

  public boolean hasSources() {
    return sources != null;
  }

  public List<String> getSources() {
    return sources;
  }

  public List<String> getSymbols() {
    return symbols;
  }

  public boolean hasSymbols() {
    return symbols != null;
  }

  public List<String> getInterpolatedSymbols() {
    return interpolatedSymbols;
  }

  public boolean hasInterpolatedSymbols() {
    return interpolatedSymbols != null;
  }

  public boolean isInterpolated(String symbolName) {
    return hasInterpolatedSymbols() && interpolatedSymbols.contains(symbolName);
  }

}