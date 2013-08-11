package com.github.sommeri.less4j.resources;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessCompiler.Configuration;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.commandline.FileSystemUtils;
import com.github.sommeri.less4j.core.DefaultLessCompiler;
import com.github.sommeri.less4j.platform.Constants;
import com.github.sommeri.less4j.utils.SourceMapValidator;

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

  public static final String ONE_IMPORT_LESS_PATH = "src/test/resources/source-map/api/file-import.less";
  public static final File ONE_IMPORT_LESS_FILE = new File(ONE_IMPORT_LESS_PATH);
  public static final String ONE_IMPORT_MAPDATA_UNKNOWN_CSS = "src/test/resources/source-map/api/file-import-unknown-css.mapdata";
  public static final String ONE_IMPORT_MAPDATA_GUESSED_CSS = "src/test/resources/source-map/api/file-import-guessed-css.mapdata";
  public static final String ONE_IMPORT_CSS_KNOWN_MAPDATA = "src/test/resources/source-map/api/file-import-css-location-known.mapdata";
  public static final String ONE_IMPORT_URL_KNOWN_MAPDATA = "src/test/resources/source-map/api/file-import-url-location-known.mapdata";

  public static final Map<String, String> LESS_INPUT_CONTENTS = new HashMap<String, String>();
  static {
    LESS_INPUT_CONTENTS.put("", NO_IMPORT_LESS_INPUT);
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
 
  //FIXME: source map - document that if css output file location is not known, the same as less file is assumend and explain why (runningon the server)
  @Test
  public void file() throws Less4jException {
    LessCompiler compiler = new DefaultLessCompiler();
    CompilationResult compilationResult = compiler.compile(ONE_IMPORT_LESS_FILE);
    
    assertNotNull(compilationResult.getCss());
    assertNotNull(compilationResult.getSourceMap());
    assertLinksSourceMap(compilationResult.getCss(), toFullMapSuffix());
    
    SourceMapValidator validator = new SourceMapValidator(LESS_INPUT_CONTENTS);
    validator.validateSourceMap(compilationResult, new File(ONE_IMPORT_MAPDATA_GUESSED_CSS), FileSystemUtils.changeSuffix(ONE_IMPORT_LESS_FILE, Constants.FULL_SOURCE_MAP_SUFFIX));
  }

  @Test
  public void fileWithEmptyConfiguration() throws Less4jException {
    LessCompiler compiler = new DefaultLessCompiler();
    CompilationResult compilationResult = compiler.compile(ONE_IMPORT_LESS_FILE, new Configuration());
    
    assertNotNull(compilationResult.getCss());
    assertNotNull(compilationResult.getSourceMap());
    assertLinksSourceMap(compilationResult.getCss(), toFullMapSuffix()); 
    
    System.out.println(compilationResult.getCss());
    System.out.println(compilationResult.getSourceMap());

    SourceMapValidator validator = new SourceMapValidator(LESS_INPUT_CONTENTS);
    validator.validateSourceMap(compilationResult, new File(ONE_IMPORT_MAPDATA_GUESSED_CSS), FileSystemUtils.changeSuffix(ONE_IMPORT_LESS_FILE, Constants.CSS_SUFFIX));
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
    System.out.println(compilationResult.getCss());
    System.out.println(compilationResult.getSourceMap());
    
    assertNotNull(compilationResult.getCss());
    assertNotNull(compilationResult.getSourceMap());
    assertLinksSourceMap(compilationResult.getCss(), FAKE_URL_RESULT_LINKED_MAP); 
    
    SourceMapValidator validator = new SourceMapValidator(LESS_INPUT_CONTENTS);
    validator.validateSourceMap(compilationResult, new File(ONE_IMPORT_URL_KNOWN_MAPDATA));
  }
  
  private void assertDoesNotLinkSourceMap(String css) {
    boolean matches = hasSourceMapLink(css);
    assertFalse("Generated css should NOT contain source map link.", matches);
  }

  private void assertLinksSourceMap(String css, String mapName) {
    assertTrue("Generated css should contain source map link.", hasSourceMapLink(css, ""));
    assertTrue("Generated css links wrong source map. Should be " + mapName + "", hasSourceMapLink(css, mapName));
  }

  private boolean hasSourceMapLink(String css) {
    return hasSourceMapLink(css, "");
  }
  
  private boolean hasSourceMapLink(String css, String mapName) {
    Pattern pattern = Pattern.compile("/\\*#\\s+sourceMappingURL="+mapName);
    Matcher matcher = pattern.matcher(css);
    return matcher.find();
  }

  private String toFullMapSuffix() {
    return FileSystemUtils.changeSuffix(ONE_IMPORT_LESS_FILE.getName(), Constants.FULL_SOURCE_MAP_SUFFIX);
  }

}
