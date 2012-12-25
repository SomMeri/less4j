package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.utils.TestFileUtils;

//FIXME: test format function with lists as parameters - focus on comma split
public class FunctionsTest extends BasicFeaturesTest {

  private static final String standardCases = "src/test/resources/compile-basic-features/functions/";

  public FunctionsTest(File inputFile, File outputFile, String testName) {
    super(inputFile, outputFile, testName);
  }

  @Parameters()
  public static Collection<Object[]> allTestsParameters() {
    return (new TestFileUtils()).loadTestFiles(standardCases);
  }

}
