package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.utils.TestFileUtils;

public class BrowserHacksTest extends BasicFeaturesTest {

  private static final String standardCases = "src/test/resources/browser-hacks/less.js-compatible/";

  public BrowserHacksTest(File inputFile, File outputFile, String testName) {
    super(inputFile, outputFile, testName);
  }

  @Parameters()
  public static Collection<Object[]> allTestsParameters() {
    return (new TestFileUtils()).loadTestFiles(standardCases);
  }

}
