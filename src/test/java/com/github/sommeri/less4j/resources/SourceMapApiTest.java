package com.github.sommeri.less4j.resources;

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessCompiler.Configuration;
import com.github.sommeri.less4j.core.DefaultLessCompiler;
import com.github.sommeri.less4j.utils.SourceMapValidator;

//FIXME: source map: add source root to map and test for it
public class SourceMapApiTest {
  
  public static final String NO_IMPORT_LESS_INPUT = ".class { color: red; }";
  public static final String NO_DATA_AVAILABLE_MAPDATA = "src/test/resources/source-map/api/no-location-available.mapdata";
  public static final String FAKE_CSS_DATA_AVAILABLE_MAPDATA = "src/test/resources/source-map/api/fake-css-available.mapdata";
  public static final String FAKE_CSS_RESULT_LOCATION = "src/test/resources/source-map/api/does-not-exists.css";
  public static final File FAKE_CSS_RESULT_FILE = new File(FAKE_CSS_RESULT_LOCATION);

  public static final String ONE_IMPORT_LESS_PATH = "src/test/resources/source-map/api/file-import.less";
  public static final File ONE_IMPORT_LESS_FILE = new File(ONE_IMPORT_LESS_PATH);
  public static final String ONE_IMPORT_MAPDATA = "src/test/resources/source-map/api/file-import.mapdata";
  public static final String ONE_IMPORT_CSS_KNOWN_MAPDATA = "src/test/resources/source-map/api/file-import-css-location-known.mapdata";

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
    
    SourceMapValidator validator = new SourceMapValidator(LESS_INPUT_CONTENTS);
    validator.validateSourceMap(compilationResult, new File(NO_DATA_AVAILABLE_MAPDATA));
  }

  @Test
  public void stringWithEmptyConfiguration() throws Less4jException {
    LessCompiler compiler = new DefaultLessCompiler();
    CompilationResult compilationResult = compiler.compile(NO_IMPORT_LESS_INPUT, new Configuration());
    
    assertNotNull(compilationResult.getCss());
    assertNotNull(compilationResult.getSourceMap());
    
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
    
    SourceMapValidator validator = new SourceMapValidator(LESS_INPUT_CONTENTS);
    validator.validateSourceMap(compilationResult, new File(FAKE_CSS_DATA_AVAILABLE_MAPDATA));
  }
 
  @Test
  public void file() throws Less4jException {
    LessCompiler compiler = new DefaultLessCompiler();
    CompilationResult compilationResult = compiler.compile(ONE_IMPORT_LESS_FILE);
    
    assertNotNull(compilationResult.getCss());
    assertNotNull(compilationResult.getSourceMap());
    
    SourceMapValidator validator = new SourceMapValidator(LESS_INPUT_CONTENTS);
    validator.validateSourceMap(compilationResult, new File(ONE_IMPORT_MAPDATA));
  }

  @Test
  public void fileWithEmptyConfiguration() throws Less4jException {
    LessCompiler compiler = new DefaultLessCompiler();
    CompilationResult compilationResult = compiler.compile(ONE_IMPORT_LESS_FILE, new Configuration());
    
    assertNotNull(compilationResult.getCss());
    assertNotNull(compilationResult.getSourceMap());
    
    SourceMapValidator validator = new SourceMapValidator(LESS_INPUT_CONTENTS);
    validator.validateSourceMap(compilationResult, new File(ONE_IMPORT_MAPDATA));
  }

  @Test
  public void fileWithConfiguration() throws Less4jException {
    Configuration configuration = new Configuration();
    configuration.setCssResultLocation(new File(FAKE_CSS_RESULT_LOCATION));

    LessCompiler compiler = new DefaultLessCompiler();
    CompilationResult compilationResult = compiler.compile(ONE_IMPORT_LESS_FILE, configuration);
    
    assertNotNull(compilationResult.getCss());
    assertNotNull(compilationResult.getSourceMap());
    
    SourceMapValidator validator = new SourceMapValidator(LESS_INPUT_CONTENTS);
    validator.validateSourceMap(compilationResult, new File(ONE_IMPORT_CSS_KNOWN_MAPDATA), FAKE_CSS_RESULT_FILE);
  }
  

  //FIXME: source map test lesssource with combinations
}
