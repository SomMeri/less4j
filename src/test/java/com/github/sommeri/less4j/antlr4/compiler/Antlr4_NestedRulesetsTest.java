package com.github.sommeri.less4j.antlr4.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

public class Antlr4_NestedRulesetsTest extends Antlr4_BasicFeaturesTest {

  private static final String standardCases = "src/test/resources/compile-basic-features/nested-rulesets/";
  private static final String lessjs = "src/test/resources/compile-basic-features/nested-rulesets/less.js/";

  public Antlr4_NestedRulesetsTest(File inputFile, File outputFile, File errorList, File mapdataFile, String testName) {
    super(inputFile, outputFile, errorList, mapdataFile, testName);
  }

  @Parameters(name="Less: {4}")
  public static Collection<Object[]> allTestsParameters() {
    return createTestFileUtils().loadTestFiles(standardCases, lessjs);
  }

}
