package com.github.sommeri.less4j;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;

//FIXME: source map: assume by default that source map, css and original less are all in the same directory
public interface LessCompiler {

  public CompilationResult compile(String lessContent) throws Less4jException;

  public CompilationResult compile(File inputFile) throws Less4jException;

  public CompilationResult compile(URL inputFile) throws Less4jException;

  public CompilationResult compile(LessSource source) throws Less4jException;

  public CompilationResult compile(File inputFile, Configuration sourceMapOptions) throws Less4jException;

  public CompilationResult compile(LessSource source, Configuration sourceMapOptions) throws Less4jException;

  public class Configuration {
    
    private LessSource cssResultLocation;
    
    /**
     * This is needed in for source map. 
     * 
     * TODO: source map: document how is this used
     */
    public LessSource getCssResultLocation() {
      return cssResultLocation;
    }

    public void setCssResultLocation(LessSource cssResultLocation) {
      this.cssResultLocation = cssResultLocation;
    }

    public void setCssResultLocation(File cssResultLocation) {
      this.cssResultLocation = new LessSource.FileSource(cssResultLocation);
    }

  }

  public class CompilationResult {

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

  public interface Problem {

    public Type getType();

    public File getFile();

    public URL getURL();

    public LessSource getSource();

    public int getLine();

    public int getCharacter();

    public String getMessage();

    public enum Type {
      WARNING, ERROR
    }

  }

}
