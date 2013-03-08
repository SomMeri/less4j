package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.AbstractErrorReportingTest;
import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.utils.TestFileUtils;

/**
 *  Testing whether less4j correctly compiles examples found in various w3c specifications 
 *  or w3c specification drafts.
 *  
 */
public class W3CSpecificationsTest extends AbstractErrorReportingTest {

  private static final String pagedMedia = "src/test/resources/w3c-specifications/CSS Paged Media Module Level 3";
  //private static final String supportsAtRule = "src/test/resources/w3c-specifications/CSS Conditional Rules Module Level 3/6 at-supports";
  
  public W3CSpecificationsTest(File lessFile, File cssOutput, File errorList, String testName) {
    super(lessFile, cssOutput, errorList, testName);
  }

  @Parameters(name="Less: {3}")
  public static Collection<Object[]> allTestsParameters() {
    return (new TestFileUtils(".err")).loadTestFiles(pagedMedia);
  }

  @Override
  protected void printErrors(Less4jException ex) {
    String errorReport = generateErrorReport(ex);
    System.err.println(errorReport);
  }

}
