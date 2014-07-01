package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import com.github.sommeri.less4j.LessCompiler;
import org.junit.runners.Parameterized.Parameters;

/**
 * A unit test for <a href="https://github.com/SomMeri/less4j/issues/215">Issue 215</a>
 */
public class CompressionTest extends BasicFeaturesTest {

  private static final String compressionCases = "src/test/resources/compile-basic-features/compression/";

  public CompressionTest(File inputFile, File outputFile, File errorList, File mapdataFile, String testName) {
    super(inputFile, outputFile, errorList, mapdataFile, testName);
  }

  @Parameters(name="Less: {4}")
  public static Collection<Object[]> allTestsParameters() {
    return createTestFileUtils().loadTestFiles(compressionCases);
  }

  @Override
  protected LessCompiler.Configuration createConfiguration(File cssOutput) {
    LessCompiler.Configuration configuration = super.createConfiguration(cssOutput);
    configuration.setCompressing(true);
    return configuration;
  }
}
