package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.utils.TestFileUtils;

//TODO write to some antlr document about token names in parser!!!
//TODO: operations-no-colors-no-mixins-lessjs.less <- check whether it started to work or not
public class ExpressionsTest extends BasicFeaturesTests {

  private static final String standardCases = "src/test/resources/compile-basic-features/expressions/";
  private static final String lessjs = "src/test/resources/compile-basic-features/expressions/less.js/";
  private static final String lessjsincompatible = "src/test/resources/compile-basic-features/expressions/less.js-incompatible/";

  public ExpressionsTest(File inputFile, File outputFile, String testName) {
    super(inputFile, outputFile, testName);
  }

  @Parameters()
  public static Collection<Object[]> allTestsParameters() {
    //return TestFileUtils.loadTestFile(standardCases+"todo/", "expressions-flying-minus.less");
    return (new TestFileUtils()).loadTestFiles(standardCases, lessjs, lessjsincompatible);
  }

}
