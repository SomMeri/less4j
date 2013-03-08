package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.AbstractFileBasedTest;
import com.github.sommeri.less4j.utils.TestFileUtils;

public class StrictComplianceTest extends AbstractFileBasedTest {

  private static final String inputDir = "src/test/resources/compile-valid-css/";

  public StrictComplianceTest(File inputFile, File outputFile, String testName) {
    super(inputFile, outputFile, testName);
  }

  //TODO: the alternative annotation is going to be useful right after jUnit 11 comes out. It will contain nicer test name.
  //@Parameters(name="Compile Less: {0}, {2}")
  @Parameters(name="Less: {2}")
  public static Collection<Object[]> allTestsParameters() {
    //return TestFileUtils.loadTestFile(inputDir, "nth-variants.less");
    return (new TestFileUtils()).loadTestFiles(inputDir);
  }

  protected String canonize(String text) {
	text = text.replace("\r\n", "\n");
    return text;
  }

}
