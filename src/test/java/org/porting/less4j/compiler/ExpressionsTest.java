package org.porting.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.porting.less4j.utils.w3ctestsextractor.TestFileUtils;

public class ExpressionsTest extends BasicFeaturesTests {

  private static final String standardCases = "src\\test\\resources\\compile-basic-features\\expressions\\";

  public ExpressionsTest(File inputFile, File outputFile, String testName) {
    super(inputFile, outputFile, testName);
  }

  @Parameters()
  public static Collection<Object[]> allTestsParameters() {
    return TestFileUtils.loadTestFiles(standardCases);
  }

}
