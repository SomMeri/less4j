package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.utils.TestFileUtils;

public class ErrorReportingTest extends AbstractErrorReportingTest {

  private static final String cases = "src/test/resources/error-handling/";

  public ErrorReportingTest(File lessFile, File partialCssFile, File errorList, String testName) {
    super(lessFile, partialCssFile, errorList, testName);
  }

  @Parameters()
  public static Collection<Object[]> allTestsParameters() {
    return (new TestFileUtils(".err")).loadTestFiles(cases);
  }
}
