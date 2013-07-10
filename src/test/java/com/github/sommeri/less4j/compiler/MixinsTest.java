package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.AbstractErrorReportingTest;

public class MixinsTest extends AbstractErrorReportingTest {
  
  //TODO: report returnMixinsLessjsIncompatible as low priority e.g., multiple-unlocks.less
  private static final String standardCases = "src/test/resources/compile-basic-features/mixins/";
  private static final String returnVariables = "src/test/resources/compile-basic-features/mixins/return-variables/";
  private static final String returnMixins = "src/test/resources/compile-basic-features/mixins/return-mixins/";
  private static final String intoNamespace = "src/test/resources/compile-basic-features/mixins/return-mixins/into-namespace/";
  private static final String returnMixinsLessjsIncompatible = "src/test/resources/compile-basic-features/mixins/return-mixins/lessjs-incompatible/";
  private static final String lessjsIncompatible = "src/test/resources/compile-basic-features/mixins/lessjs-incompatible";
  private static final String lessjsTests = "src/test/resources/compile-basic-features/mixins/less.js";

  public MixinsTest(File inputFile, File outputFile, File errorList, File mapdataFile, String testName) {
    super(inputFile, outputFile, errorList, mapdataFile, testName);
  }

  @Parameters(name="Less: {4}")
  public static Collection<Object[]> allTestsParameters() {
    return createTestFileUtils().loadTestFiles(intoNamespace, standardCases, returnVariables, returnMixins, returnMixinsLessjsIncompatible, lessjsIncompatible, lessjsTests);
  }

}
