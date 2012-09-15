package org.porting.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.porting.less4j.utils.w3ctestsextractor.TestFileUtils;

//TODO write to some antlr document about token names in parser!!!
//TODO less.js translates   something: (12) (- 23) into something: 12 - 23 - maybe it is a bug: should be -23 (we do that)
//TODO less.js fails on  font:14px+16px; and font:14px*16px;

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
