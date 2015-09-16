package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

public class CssOptimizationsTest extends BasicFeaturesTest {

  private static final String standardCases = "src/test/resources/compile-basic-features/css-optimizations/";

  public CssOptimizationsTest(File inputFile, File outputFile, File errorList, File mapdataFile, File configFile, String testName) {
    super(inputFile, outputFile, errorList, mapdataFile, configFile, testName);
  }

  @Parameters(name="Less: {4}")
  public static Collection<Object[]> allTestsParameters() {
    return createTestFileUtils().loadTestFiles(standardCases);
  }

}
