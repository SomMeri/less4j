package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

public class NamespacesTest extends BasicFeaturesTest {
  
  private static final String standardCases = "src/test/resources/compile-basic-features/namespaces/";
  private static final String lessjsIncompatible = "src/test/resources/compile-basic-features/namespaces/lessjs-incompatible";

  public NamespacesTest(File inputFile, File outputFile, File errorList, File mapdataFile, File configFile, String testName) {
    super(inputFile, outputFile, errorList, mapdataFile, configFile, testName);
  }

  @Parameters(name="Less: {5}")
  public static Collection<Object[]> allTestsParameters() {
    //return createTestFileUtils().loadTestFile(standardCases+"todo/", "namespaces-scoping-mixins-A.less");
    return createTestFileUtils().loadTestFiles(standardCases, lessjsIncompatible);
  }

}
