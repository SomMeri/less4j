package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

//FIXME: !!!!!!!!!!! default-shoud-pass-lessjs-incompatible.less
//FIXME: !!!!!!!!!!! file.less
//FIXME: !!!!!!!!!!! less.js issue 1800
public class FunctionsTest extends BasicFeaturesTest {

  private static final String standardCases = "src/test/resources/compile-basic-features/functions/";

  public FunctionsTest(File inputFile, File outputFile, File errorList, File mapdataFile, String testName) {
    super(inputFile, outputFile, errorList, mapdataFile, testName);
  }

  @Parameters(name="Less: {4}")
  public static Collection<Object[]> allTestsParameters() {
    return createTestFileUtils().loadTestFiles(standardCases);
  }

}
