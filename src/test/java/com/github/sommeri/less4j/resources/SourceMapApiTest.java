package com.github.sommeri.less4j.resources;

import static com.github.sommeri.less4j.resources.SourceMapLinkParser.assertDoesNotLinkSourceMap;
import static com.github.sommeri.less4j.resources.SourceMapLinkParser.assertInlineSourceMap;
import static com.github.sommeri.less4j.resources.SourceMapLinkParser.assertLinksSourceMap;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessCompiler.Configuration;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.DefaultLessCompiler;
import com.github.sommeri.less4j.platform.Constants;
import com.github.sommeri.less4j.utils.SourceMapValidator;
import com.github.sommeri.less4j.utils.URIUtils;

public class SourceMapApiTest {
  
  public static final String NO_IMPORT_LESS_INPUT = ".class { color: red; }";
  public static final String NO_DATA_AVAILABLE_MAPDATA = "src/test/resources/source-map/api/no-location-available.mapdata";
  public static final String FAKE_CSS_DATA_AVAILABLE_MAPDATA = "src/test/resources/source-map/api/fake-css-available.mapdata";
  public static final String FAKE_URL_DATA_AVAILABLE_MAPDATA = "src/test/resources/source-map/api/fake-url-available.mapdata";
  public static final String FAKE_CSS_RESULT_LOCATION = "src/test/resources/source-map/api/does-not-exists.css";
  public static final String FAKE_CSS_RESULT_LINKED_MAP = "does-not-exists.css.map";
  public static final File FAKE_CSS_RESULT_FILE = new File(FAKE_CSS_RESULT_LOCATION);

  public static final String FAKE_URL_RESULT_LOCATION = "http://www.somewhere.com/dummy.html#here?parameter=value";
  public static final String FAKE_URL_RESULT_CSS = "dummy.html";
  public static final String FAKE_URL_RESULT_LINKED_MAP = "dummy.html.map";
  public static final URL FAKE_URL_RESULT_URL = toUrl();
  
  private static URL toUrl() {
    try {
      return new URL(FAKE_URL_RESULT_LOCATION);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  private static final String ONE_IMPORT_LESS_PATH = "src/test/resources/source-map/api/file-import.less";
  private static final File ONE_IMPORT_LESS_FILE = new File(ONE_IMPORT_LESS_PATH);
  private static final String ONE_IMPORT_MAPDATA_GUESSED_CSS = "src/test/resources/source-map/api/file-import-guessed-css.mapdata";
  private static final String ONE_IMPORT_MAPDATA_WITH_LESS = "src/test/resources/source-map/api/file-import-with-less.mapdata";
  private static final String ONE_IMPORT_MAPDATA_NO_RELATIVIZATION = "src/test/resources/source-map/api/file-import-no-path-relativization.mapdata";
  private static final String ONE_IMPORT_CSS_KNOWN_MAPDATA = "src/test/resources/source-map/api/file-import-css-location-known.mapdata";
  private static final String ONE_IMPORT_URL_KNOWN_MAPDATA = "src/test/resources/source-map/api/file-import-url-location-known.mapdata";

  public static final Map<String, String> LESS_INPUT_CONTENTS = new HashMap<String, String>();
  static {
    LESS_INPUT_CONTENTS.put(null, NO_IMPORT_LESS_INPUT);
  }
  
  @Test
  public void string() throws Less4jException {
    LessCompiler compiler = new DefaultLessCompiler();
    CompilationResult compilationResult = compiler.compile(NO_IMPORT_LESS_INPUT);
    
    assertNotNull(compilationResult.getCss());
    assertNotNull(compilationResult.getSourceMap());
    assertDoesNotLinkSourceMap(compilationResult.getCss());
    
    SourceMapValidator validator = new SourceMapValidator(LESS_INPUT_CONTENTS);
    validator.validateSourceMap(compilationResult, new File(NO_DATA_AVAILABLE_MAPDATA));
  }

  @Test
  public void stringWithEmptyConfiguration() throws Less4jException {
    LessCompiler compiler = new DefaultLessCompiler();
    CompilationResult compilationResult = compiler.compile(NO_IMPORT_LESS_INPUT, new Configuration());
    
    assertNotNull(compilationResult.getCss());
    assertNotNull(compilationResult.getSourceMap());
    assertDoesNotLinkSourceMap(compilationResult.getCss());
    
    SourceMapValidator validator = new SourceMapValidator(LESS_INPUT_CONTENTS);
    validator.validateSourceMap(compilationResult, new File(NO_DATA_AVAILABLE_MAPDATA));
  }

  @Test
  public void stringWithSourceMap() throws Less4jException {
    LessCompiler compiler = new DefaultLessCompiler();
    Configuration options = new Configuration();
    options.getSourceMapConfiguration().setInline(true);
    CompilationResult compilationResult = compiler.compile(NO_IMPORT_LESS_INPUT, options);
    
    assertNotNull(compilationResult.getCss());
    assertNotNull(compilationResult.getSourceMap());
    assertInlineSourceMap(compilationResult);
    
    SourceMapValidator validator = new SourceMapValidator(LESS_INPUT_CONTENTS);
    validator.validateSourceMap(compilationResult, new File(NO_DATA_AVAILABLE_MAPDATA));
  }

  @Test
  public void stringWithConfiguration() throws Less4jException {
    Configuration configuration = new Configuration();
    configuration.setCssResultLocation(FAKE_CSS_RESULT_FILE);
    
    LessCompiler compiler = new DefaultLessCompiler();
    CompilationResult compilationResult = compiler.compile(NO_IMPORT_LESS_INPUT, configuration);
    
    assertNotNull(compilationResult.getCss());
    assertNotNull(compilationResult.getSourceMap());
    assertLinksSourceMap(compilationResult.getCss(), FAKE_CSS_RESULT_LINKED_MAP);

    SourceMapValidator validator = new SourceMapValidator(LESS_INPUT_CONTENTS);
    validator.validateSourceMap(compilationResult, new File(FAKE_CSS_DATA_AVAILABLE_MAPDATA));
  }
 
  @Test
  public void stringWithURLConfiguration() throws Less4jException {
    Configuration configuration = new Configuration();
    configuration.setCssResultLocation(new LessSource.URLSource(FAKE_URL_RESULT_URL));
    
    LessCompiler compiler = new DefaultLessCompiler();
    CompilationResult compilationResult = compiler.compile(NO_IMPORT_LESS_INPUT, configuration);
    
    assertNotNull(compilationResult.getCss());
    assertNotNull(compilationResult.getSourceMap());
    assertLinksSourceMap(compilationResult.getCss(), FAKE_URL_RESULT_LINKED_MAP);

    SourceMapValidator validator = new SourceMapValidator(LESS_INPUT_CONTENTS);
    validator.validateSourceMap(compilationResult, new File(FAKE_URL_DATA_AVAILABLE_MAPDATA));
  }
 
  @Test
  public void file() throws Less4jException {
    LessCompiler compiler = new DefaultLessCompiler();
    CompilationResult compilationResult = compiler.compile(ONE_IMPORT_LESS_FILE);
    
    assertNotNull(compilationResult.getCss());
    assertNotNull(compilationResult.getSourceMap());
    assertLinksSourceMap(compilationResult.getCss(), toFullMapSuffix());
    
    SourceMapValidator validator = new SourceMapValidator(LESS_INPUT_CONTENTS);
    validator.validateSourceMap(compilationResult, new File(ONE_IMPORT_MAPDATA_GUESSED_CSS), URIUtils.changeSuffix(ONE_IMPORT_LESS_FILE, Constants.FULL_SOURCE_MAP_SUFFIX));
  }

  @Test
  public void fileWithEmptyConfiguration() throws Less4jException {
    LessCompiler compiler = new DefaultLessCompiler();
    CompilationResult compilationResult = compiler.compile(ONE_IMPORT_LESS_FILE, new Configuration());
    
    assertNotNull(compilationResult.getCss());
    assertNotNull(compilationResult.getSourceMap());
    assertLinksSourceMap(compilationResult.getCss(), toFullMapSuffix()); 
    
    SourceMapValidator validator = new SourceMapValidator(LESS_INPUT_CONTENTS);
    validator.validateSourceMap(compilationResult, new File(ONE_IMPORT_MAPDATA_GUESSED_CSS), URIUtils.changeSuffix(ONE_IMPORT_LESS_FILE, Constants.CSS_SUFFIX));
  }

  @Test
  public void fileDoNotRelativize() throws Less4jException {
    LessCompiler compiler = new DefaultLessCompiler();
    Configuration configuration = new Configuration(); 
    configuration.getSourceMapConfiguration().setRelativizePaths(false);
    CompilationResult compilationResult = compiler.compile(ONE_IMPORT_LESS_FILE, configuration);
    
    assertNotNull(compilationResult.getCss());
    assertNotNull(compilationResult.getSourceMap());
    assertLinksSourceMap(compilationResult.getCss(), toFullMapSuffix());
    
    SourceMapValidator validator = new SourceMapValidator("");
    validator.validateSourceMap(compilationResult, new File(ONE_IMPORT_MAPDATA_NO_RELATIVIZATION), URIUtils.changeSuffix(ONE_IMPORT_LESS_FILE, Constants.CSS_SUFFIX));
  }

  @Test
  public void fileIncludeLessFiles() throws Less4jException {
    LessCompiler compiler = new DefaultLessCompiler();
    Configuration configuration = new Configuration(); 
    configuration.getSourceMapConfiguration().setIncludeSourcesContent(true);
    CompilationResult compilationResult = compiler.compile(ONE_IMPORT_LESS_FILE, configuration);
    
    assertNotNull(compilationResult.getCss());
    assertNotNull(compilationResult.getSourceMap());
    assertLinksSourceMap(compilationResult.getCss(), toFullMapSuffix());
    
    SourceMapValidator validator = new SourceMapValidator(LESS_INPUT_CONTENTS);
    validator.validateSourceMap(compilationResult, new File(ONE_IMPORT_MAPDATA_WITH_LESS), URIUtils.changeSuffix(ONE_IMPORT_LESS_FILE, Constants.CSS_SUFFIX));
  }

  @Test
  public void fileSelfContained() throws Less4jException {
    LessCompiler compiler = new DefaultLessCompiler();
    Configuration configuration = new Configuration(); 
    configuration.getSourceMapConfiguration().setIncludeSourcesContent(true);
    configuration.getSourceMapConfiguration().setInline(true);
    CompilationResult compilationResult = compiler.compile(ONE_IMPORT_LESS_FILE, configuration);
    
    assertNotNull(compilationResult.getCss());
    assertNotNull(compilationResult.getSourceMap());
    assertInlineSourceMap(compilationResult);
    
    SourceMapValidator validator = new SourceMapValidator(LESS_INPUT_CONTENTS);
    validator.validateSourceMap(compilationResult, new File(ONE_IMPORT_MAPDATA_WITH_LESS), URIUtils.changeSuffix(ONE_IMPORT_LESS_FILE, Constants.CSS_SUFFIX));
  }

  @Test
  public void fileWithConfiguration() throws Less4jException {
    Configuration configuration = new Configuration();
    configuration.setCssResultLocation(new File(FAKE_CSS_RESULT_LOCATION));

    LessCompiler compiler = new DefaultLessCompiler();
    CompilationResult compilationResult = compiler.compile(ONE_IMPORT_LESS_FILE, configuration);
    
    assertNotNull(compilationResult.getCss());
    assertNotNull(compilationResult.getSourceMap());
    assertLinksSourceMap(compilationResult.getCss(), FAKE_CSS_RESULT_LINKED_MAP); 
    
    SourceMapValidator validator = new SourceMapValidator(LESS_INPUT_CONTENTS);
    validator.validateSourceMap(compilationResult, new File(ONE_IMPORT_CSS_KNOWN_MAPDATA), FAKE_CSS_RESULT_FILE);
  }
  
  @Test
  public void fileWithUrlConfiguration() throws Less4jException {
    Configuration configuration = new Configuration();
    configuration.setCssResultLocation(new LessSource.URLSource(FAKE_URL_RESULT_URL));

    LessCompiler compiler = new DefaultLessCompiler();
    CompilationResult compilationResult = compiler.compile(ONE_IMPORT_LESS_FILE, configuration);
    
    assertNotNull(compilationResult.getCss());
    assertNotNull(compilationResult.getSourceMap());
    assertLinksSourceMap(compilationResult.getCss(), FAKE_URL_RESULT_LINKED_MAP); 
    
    SourceMapValidator validator = new SourceMapValidator(LESS_INPUT_CONTENTS);
    validator.validateSourceMap(compilationResult, new File(ONE_IMPORT_URL_KNOWN_MAPDATA));
  }
  
  private String toFullMapSuffix() {
    return URIUtils.changeSuffix(ONE_IMPORT_LESS_FILE.getName(), Constants.FULL_SOURCE_MAP_SUFFIX);
  }

}
