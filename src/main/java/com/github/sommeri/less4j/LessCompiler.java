package com.github.sommeri.less4j;

import com.github.sommeri.sourcemap.DefaultSourceMapUrlGenerator;
import com.github.sommeri.sourcemap.SourceMapUrlGenerator;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface LessCompiler {

  public CompilationResult compile(String lessContent) throws Less4jException;

  public CompilationResult compile(String lessContent, Configuration options) throws Less4jException;

  public CompilationResult compile(File lessFile) throws Less4jException;

  public CompilationResult compile(File lessFile, Configuration options) throws Less4jException;

  public CompilationResult compile(URL lessUrl) throws Less4jException;

  public CompilationResult compile(URL lessUrl, Configuration options) throws Less4jException;

  public CompilationResult compile(LessSource source) throws Less4jException;

  public CompilationResult compile(LessSource source, Configuration options) throws Less4jException;

  /**
   * WARNING: experimental API
   */
  public static class Configuration {

    private LessSource cssResultLocation;
    private SourceMapConfiguration sourceMapConfiguration = new SourceMapConfiguration();
    private List<LessFunction> functionPackages = new ArrayList<LessFunction>();
    private Map<String, String> externalVariables = new HashMap<String, String>();
    private EmbeddedScriptGenerator embeddedScriptGenerator;
    private boolean compressing = false;
    private boolean ieCompatibility = true;
    private Cache cache;

    /**
     * This is needed in for source map.
     * 
     */
    public LessSource getCssResultLocation() {
      return cssResultLocation;
    }

    public void setCssResultLocation(LessSource cssResultLocation) {
      this.cssResultLocation = cssResultLocation;
    }

    public void setCssResultLocation(File cssResultLocation) {
      this.cssResultLocation = cssResultLocation == null ? null : new LessSource.FileSource(cssResultLocation);
    }

    /**
     * @deprecated Use getSourceMapConfiguration().shouldLinkSourceMap() instead
     */
    @Deprecated
    public boolean shouldLinkSourceMap() {
      return sourceMapConfiguration.shouldLinkSourceMap();
    }

    /**
     * @deprecated Use getSourceMapConfiguration().setLinkSourceMap(boolean)
     *             instead
     */
    public void setLinkSourceMap(boolean linkSourceMap) {
      sourceMapConfiguration.setLinkSourceMap(linkSourceMap);
    }

    public List<LessFunction> getCustomFunctions() {
      return functionPackages;
    }

    public void addCustomFunctions(List<LessFunction> functionPackages) {
      this.functionPackages.addAll(functionPackages);
    }

    public void addCustomFunction(LessFunction functionPackage) {
      this.functionPackages.add(functionPackage);
    }

    public void addExternalVariables(Map<String, String> variables) {
      this.externalVariables.putAll(variables);
    }

    public void addExternalVariable(String name, String value) {
      this.externalVariables.put(name, value);
    }

    public Map<String, String> getVariables() {
      return externalVariables;
    }

    public EmbeddedScriptGenerator getEmbeddedScriptGenerator() {
      return embeddedScriptGenerator;
    }

    public void setEmbeddedScriptGenerator(EmbeddedScriptGenerator embeddedScripting) {
      this.embeddedScriptGenerator = embeddedScripting;
    }

    public SourceMapConfiguration getSourceMapConfiguration() {
      return sourceMapConfiguration;
    }

    public Configuration setCompressing(boolean compressing) {
      this.compressing = compressing;
      return this;
    }

    public boolean isCompressing() {
      return compressing;
    }

    /**
     * Turns off and on ie-compatible limitations. Currently used only inside data-uri 
     * function. If it is on, data-uri wont encode images too large for ie8. Turning 
     * it off is equivalent to calling less.js with --no-ie-compat option.
     */
    public boolean hasIeCompatibility() {
      return ieCompatibility;
    }

    /**
     * Turns off and on ie-compatible limitations. Currently used only inside data-uri 
     * function. If it is on, data-uri wont encode images too large for ie8. Turning 
     * it off is equivalent to calling less.js with --no-ie-compat option.
     */
    public void setIeCompatibility(boolean ieCompatibility) {
      this.ieCompatibility = ieCompatibility;
    }

    public Cache getCache() {
      return cache;
    }

    public Configuration setCache(Cache cache) {
      this.cache = cache;
      return this;
    }

  }

  public static class SourceMapConfiguration {
    private boolean linkSourceMap = true;
    private boolean inline = false;
    private String encodingCharset = "UTF-8";
    private boolean relativizePaths = true;
    private boolean includeSourcesContent = false;
    private SourceMapUrlGenerator sourceMapNameGenerator = new DefaultSourceMapUrlGenerator();

    public boolean shouldLinkSourceMap() {
      return linkSourceMap;
    }

    /**
     * If set to <code>false</code>, generated css does not contain link to
     * source map file.
     */
    public SourceMapConfiguration setLinkSourceMap(boolean linkSourceMap) {
      this.linkSourceMap = linkSourceMap;
      return this;
    }

    public boolean isInline() {
      return inline;
    }

    /**
     * If set to <code>true</code>, whole source map is encoded and embedded
     * into generated css. It is <code>false</code> by default.
     */
    public SourceMapConfiguration setInline(boolean inline) {
      this.inline = inline;
      return this;
    }

    public String getEncodingCharset() {
      return encodingCharset;
    }

    /**
     * Source map and source map link encoding charset.
     */
    public SourceMapConfiguration setEncodingCharset(String encodingCharset) {
      this.encodingCharset = encodingCharset;
      return this;
    }

    public boolean isRelativizePaths() {
      return relativizePaths;
    }

    /**
     * If set to false, final source map contains unmodified (absolute) paths to
     * original less files. If set to true, generated map contains relative
     * paths. Note that "correct" source map should contain relative paths. Use
     * this option only if you need some kind of post processing on generated
     * map.
     */
    public SourceMapConfiguration setRelativizePaths(boolean relativizePaths) {
      this.relativizePaths = relativizePaths;
      return this;
    }

    public boolean isIncludeSourcesContent() {
      return includeSourcesContent;
    }

    /**
     * If set to <code>true</code>, content of compiled (source) files is
     * included inside source map. Source map is independent of compiled less
     * files locations.
     */
    public SourceMapConfiguration setIncludeSourcesContent(boolean includeSourcesContent) {
      this.includeSourcesContent = includeSourcesContent;
      return this;
    }

    public SourceMapUrlGenerator getSourceMapNameGenerator() {
      return sourceMapNameGenerator;
    }

    public SourceMapConfiguration setSourceMapNameGenerator(SourceMapUrlGenerator sourceMapNameGenerator) {
      this.sourceMapNameGenerator = sourceMapNameGenerator != null ? sourceMapNameGenerator : new DefaultSourceMapUrlGenerator();
      return this;
    }
  }

  public static class CompilationResult {

    private final String css;
    private final String sourceMap;
    private final List<Problem> warnings;

    public CompilationResult(String css) {
      this(css, "", emptyList());
    }

    private static List<Problem> emptyList() {
      return Collections.emptyList();
    }

    public CompilationResult(String css, String sourceMap, List<Problem> warnings) {
      super();
      this.css = css;
      this.sourceMap = sourceMap;
      this.warnings = warnings;
    }

    public String getCss() {
      return css;
    }

    public List<Problem> getWarnings() {
      return warnings;
    }

    public String getSourceMap() {
      return sourceMap;
    }

  }

  /**
   * Cache with objects to be reused between runs. Use this to speed up compilation
   * when you multiple sheets import and use the same less files. Use with caution, 
   * may cause faults if imported data change between consecutive runs.   
   *
   */
  public interface Cache {

    /**
     * Get ast corresponding to parsed source.  
     */
    Object getAst(LessSource key);

    /**
     * Set ast corresponding to parsed source.  
     */
    void setAst(LessSource key, Object value);
  }

  public interface Problem {

    public Type getType();

    public LessSource getSource();

    public int getLine();

    public int getCharacter();

    public String getMessage();

    public enum Type {
      WARNING, ERROR
    }

  }

}
