package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.AbstractFileBasedTest;

public class ErrorReportingTest extends AbstractFileBasedTest {

  private static final String basicCases = "src/test/resources/error-handling/";
  private static final String functionsCases = "src/test/resources/error-handling/functions/";
  private static final String deprecatedWarnings = "src/test/resources/error-handling/deprecated-warnings/";
  private static final String strict = "src/test/resources/error-handling/malformed-less/";

  public ErrorReportingTest(File inputFile, File outputFile, File errorList, File mapdataFile, File configFile, String testName) {
    super(inputFile, outputFile, errorList, mapdataFile, configFile, testName);
  }

  @Parameters(name="Less: {4}")
  public static Collection<Object[]> allTestsParameters() {
    return createTestFileUtils().loadTestFiles(basicCases, functionsCases, deprecatedWarnings, strict);
  }
}
