package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.AbstractFileBasedTest;
import com.github.sommeri.less4j.Less4jException;

public class MediaTest extends AbstractFileBasedTest {

  private static final String standardCases = "src/test/resources/compile-basic-features/media/";

  public MediaTest(File inputFile, File outputFile, File errorList, File mapdataFile, String testName) {
    super(inputFile, outputFile, errorList, mapdataFile, testName);
  }

  @Parameters(name="Less: {4}")
  public static Collection<Object[]> allTestsParameters() {
    return createTestFileUtils().loadTestFiles(standardCases);
  }

  @Override
  protected void printErrors(Less4jException ex) {
    String errorReport = generateErrorReport(ex);
    System.err.println(errorReport);
  }
}
