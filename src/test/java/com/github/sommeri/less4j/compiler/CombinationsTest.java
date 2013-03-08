package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.AbstractErrorReportingTest;
import com.github.sommeri.less4j.utils.TestFileUtils;

public class CombinationsTest extends AbstractErrorReportingTest {

  private static final String standardCases = "src/test/resources/compile-basic-features/combinations/";

  public CombinationsTest(File inputFile, File outputFile, File errorList, String testName) {
    super(inputFile, outputFile, errorList, testName);
  }

  @Parameters(name="Less: {3}")
  public static Collection<Object[]> allTestsParameters() {
    return (new TestFileUtils(".err")).loadTestFiles(standardCases);
  }

}
