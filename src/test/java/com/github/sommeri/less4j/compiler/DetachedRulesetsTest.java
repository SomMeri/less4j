package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

/*
 * TODO: test if @defaults works correctly -- e.g. including various callers scopes
*  TODO: test order detached mixin imports who sees who and who overwrites who
*  FIXME: !!!!!!!!!!!!! test mixin returned from detached ruleset
 */
public class DetachedRulesetsTest extends BasicFeaturesTest {

  private static final String standardCases = "src/test/resources/compile-basic-features/detached-rulesets/";

  public DetachedRulesetsTest(File inputFile, File outputFile, File errorList, File mapdataFile, String testName) {
    super(inputFile, outputFile, errorList, mapdataFile, testName);
  }

  @Parameters(name="Less: {4}")
  public static Collection<Object[]> allTestsParameters() {
    return createTestFileUtils().loadTestFiles(standardCases);
  }

}
