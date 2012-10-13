package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.utils.w3ctestsextractor.TestFileUtils;

//TODO: document css pasring issues and solutions
public class NestedRulesetsTest extends BasicFeaturesTests {

  private static final String standardCases = "src/test/resources/compile-basic-features/nested-rulesets/";
  private static final String lessjs = "src/test/resources/compile-basic-features/nested-rulesets/less.js/";

  public NestedRulesetsTest(File inputFile, File outputFile, String testName) {
    super(inputFile, outputFile, testName);
  }

  @Parameters()
  public static Collection<Object[]> allTestsParameters() {
    return (new TestFileUtils()).loadTestFiles(standardCases, lessjs);
  }

}
