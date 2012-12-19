package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.utils.TestFileUtils;

public class ErrorReportingTest extends AbstractErrorReportingTest {

  private static final String basicCases = "src/test/resources/error-handling/";
  private static final String functionsCases = "src/test/resources/error-handling/functions/";
  private static final String deprecatedWarnings = "src/test/resources/error-handling/deprecated-warnings/";
  private static final String strict = "src/test/resources/error-handling/malformed-less/";

  public ErrorReportingTest(File lessFile, File partialCssFile, File errorList, String testName) {
    super(lessFile, partialCssFile, errorList, testName);
  }

  @Parameters()
  public static Collection<Object[]> allTestsParameters() {
    return (new TestFileUtils(".err")).loadTestFiles(basicCases, functionsCases, deprecatedWarnings, strict);
  }
}
