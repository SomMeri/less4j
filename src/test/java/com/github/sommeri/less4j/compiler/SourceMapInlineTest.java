package com.github.sommeri.less4j.compiler;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.AbstractFileBasedTest;
import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessCompiler.Configuration;

public class SourceMapInlineTest extends AbstractFileBasedTest {

  private static final Pattern SOURCE_MAP_PATTERN = Pattern.compile("/\\*# sourceMappingURL=.*", Pattern.DOTALL);
  private static final String standardCases = "src/test/resources/source-map/inline/";

  public SourceMapInlineTest(File inputFile, File outputFile, File errorList, File mapdataFile, String testName) {
    super(inputFile, outputFile, errorList, mapdataFile, testName);
  }

  protected Configuration createConfiguration(File cssOutput) {
    Configuration configuration = super.createConfiguration(cssOutput);
    configuration.getSourceMapConfiguration().setInline(true);
    return configuration;
  }

  protected void assertSourceMapValid(CompilationResult actual) {
    super.assertSourceMapValid(actual);
    String sourceMap = extractAttachedSourceMap(actual.getCss());
    assertEquals("Attached source map should be the same as generated one.", actual.getSourceMap(), sourceMap);
  }

  private String extractAttachedSourceMap(String css) {
    Matcher matcher = SOURCE_MAP_PATTERN.matcher(css);
    if (!matcher.find())
      fail("Source map is missing.");

    String sourceMapComment = matcher.group();
    String encodedSourceMap = sourceMapComment.substring(sourceMapComment.lastIndexOf(",") + 1, sourceMapComment.lastIndexOf(" */") + 1);
    String sourceMap = new String(DatatypeConverter.parseBase64Binary(encodedSourceMap));
    return sourceMap;
  }

  @Parameters(name = "Less: {4}")
  public static Collection<Object[]> allTestsParameters() {
    return createTestFileUtils().loadTestFiles(standardCases);
  }

  protected String canonize(String text) {
    text = super.canonize(text);

    text = SOURCE_MAP_PATTERN.matcher(text).replaceAll("");

    //ignore occasional end lines
    while (text.endsWith("\n"))
      text = text.substring(0, text.length() - 1);

    return text;
  }

}
