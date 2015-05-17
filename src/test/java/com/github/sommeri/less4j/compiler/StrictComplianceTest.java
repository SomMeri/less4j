package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.AbstractFileBasedTest;

public class StrictComplianceTest extends AbstractFileBasedTest {

  private static final String inputDir = "src/test/resources/compile-valid-css/";

  public StrictComplianceTest(File inputFile, File outputFile, File errorList, File mapdataFile, String testName) {
    super(inputFile, outputFile, errorList, mapdataFile, testName);
  }

  @Parameters(name="Less: {4}")
  public static Collection<Object[]> allTestsParameters() {
    return createTestFileUtils().loadTestFiles(inputDir);
  }

  protected String canonize(String text) {
	text = text.replace("\r\n", "\n");
    return text;
  }

}
