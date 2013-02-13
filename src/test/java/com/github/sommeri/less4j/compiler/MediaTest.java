package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.utils.TestFileUtils;

public class MediaTest extends AbstractErrorReportingTest {

  private static final String standardCases = "src/test/resources/compile-basic-features/media/";

  public MediaTest(File lessFile, File cssOutput, File errorList, String testName) {
    super(lessFile, cssOutput, errorList, testName);
  }

  @Parameters()
  public static Collection<Object[]> allTestsParameters() {
    return (new TestFileUtils(".err")).loadTestFiles(standardCases);
  }

}
