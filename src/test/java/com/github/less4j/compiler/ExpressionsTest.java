package com.github.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

import com.github.less4j.utils.w3ctestsextractor.TestFileUtils;

//TODO write to some antlr document about token names in parser!!!
public class ExpressionsTest extends BasicFeaturesTests {

  private static final String standardCases = "src/test/resources/compile-basic-features/expressions/";

  public ExpressionsTest(File inputFile, File outputFile, String testName) {
    super(inputFile, outputFile, testName);
  }

  @Parameters()
  public static Collection<Object[]> allTestsParameters() {
    //return TestFileUtils.loadTestFile(standardCases+"todo/", "expressions-flying-minus.less");
    return TestFileUtils.loadTestFiles(standardCases);
  }

}
