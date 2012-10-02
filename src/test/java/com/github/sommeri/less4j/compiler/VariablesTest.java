package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.utils.w3ctestsextractor.TestFileUtils;

public class VariablesTest extends BasicFeaturesTests {

  private static final String standardCases = "src/test/resources/compile-basic-features/variables/";

  public VariablesTest(File inputFile, File outputFile, String testName) {
    super(inputFile, outputFile, testName);
  }

  @Parameters()
  public static Collection<Object[]> allTestsParameters() {
    return TestFileUtils.loadTestFiles(standardCases);
  }

}
