package org.porting.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.porting.less4j.utils.w3ctestsextractor.TestFileUtils;

//TODO: document css pasring issues and solutions
//TODO: test nested selectors inside media declaration 
public class NestedRulesetsTest extends BasicFeaturesTests {

  private static final String standardCases = "src\\test\\resources\\compile-basic-features\\nested-rulesets\\";

  public NestedRulesetsTest(File inputFile, File outputFile, String testName) {
    super(inputFile, outputFile, testName);
  }

  @Parameters()
  public static Collection<Object[]> allTestsParameters() {
    return TestFileUtils.loadTestFiles(standardCases);
  }

}
