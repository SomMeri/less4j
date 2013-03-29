package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.AbstractErrorReportingTest;
import com.github.sommeri.less4j.utils.TestFileUtils;

public class MixinsTest extends AbstractErrorReportingTest {
  
  private static final String standardCases = "src/test/resources/compile-basic-features/mixins/";
  private static final String returnValue = "src/test/resources/compile-basic-features/mixins/return-value/";
  private static final String lessjsIncompatible = "src/test/resources/compile-basic-features/mixins/lessjs-incompatible";
  private static final String lessjsTests = "src/test/resources/compile-basic-features/mixins/less.js";

  public MixinsTest(File lessFile, File cssOutput, File errorList, String testName) {
    super(lessFile, cssOutput, errorList, testName);
  }

  @Parameters(name="Less: {3}")
  public static Collection<Object[]> allTestsParameters() {
    return (new TestFileUtils(".err")).loadTestFiles(standardCases, returnValue, lessjsIncompatible, lessjsTests);
  }

}
