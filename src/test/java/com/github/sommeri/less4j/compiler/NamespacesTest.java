package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.utils.w3ctestsextractor.TestFileUtils;

public class NamespacesTest extends BasicFeaturesTests {
  
  private static final String standardCases = "src/test/resources/compile-basic-features/namespaces/";
//  private static final String lessjsIncompatible = "src/test/resources/compile-basic-features/mixins/lessjs-incompatible";
//  private static final String lessjsTests = "src/test/resources/compile-basic-features/mixins/less.js";

  public NamespacesTest(File inputFile, File outputFile, String testName) {
    super(inputFile, outputFile, testName);
  }

  @Parameters()
  public static Collection<Object[]> allTestsParameters() {
    return (new TestFileUtils()).loadTestFiles(standardCases);
  }

}
