package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

public class ExpressionsTest extends BasicFeaturesTest {

  private static final String standardCases = "src/test/resources/compile-basic-features/expressions/";
  private static final String lessjs = "src/test/resources/compile-basic-features/expressions/less.js/";
  private static final String lessjsincompatible = "src/test/resources/compile-basic-features/expressions/less.js-incompatible/";

  public ExpressionsTest(File inputFile, File outputFile, File errorList, File mapdataFile, File configFile, String testName) {
    super(inputFile, outputFile, errorList, mapdataFile, configFile, testName);
  }

  @Parameters(name="Less: {5}")
  public static Collection<Object[]> allTestsParameters() {
    //return createTestFileUtils().loadTestFile(standardCases+"todo/", "expressions-flying-minus.less");
    return createTestFileUtils().loadTestFiles(standardCases, lessjs, lessjsincompatible);
  }

}
