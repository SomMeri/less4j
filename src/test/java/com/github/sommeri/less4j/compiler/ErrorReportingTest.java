package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.utils.TestFileUtils;

//@Ignore //not really sure yet how do I want to handle errors
//public class ErrorReportingTest extends AbstractErrorReportingTest {
//FIXME: test whether errors are reported correctly
public class ErrorReportingTest extends BasicFeaturesTest {

  private static final String cases = "src/test/resources/error-handling/";

  public ErrorReportingTest(File lessFile, File errorList, String testName) {
    super(lessFile, errorList, testName);
  }

  @Parameters()
  public static Collection<Object[]> allTestsParameters() {
    //return (new TestFileUtils(".txt")).loadTestFiles(cases);
    return (new TestFileUtils()).loadTestFiles(cases);
  }
}
