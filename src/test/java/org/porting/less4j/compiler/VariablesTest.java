package org.porting.less4j.compiler;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.porting.less4j.utils.w3ctestsextractor.TestFileUtils;

//TODO: document variables lazy loading and last win strategies; link two issues
public class VariablesTest extends BasicFeaturesTests {

  private static final String standardCases = "src\\test\\resources\\compile-basic-features\\variables\\";

  public VariablesTest(File inputFile, File outputFile, String testName) {
    super(inputFile, outputFile, testName);
  }

  @Parameters()
  public static Collection<Object[]> allTestsParameters() {
    try {
      System.out.println("Travis Directory Experiment: |" + new java.io.File(".").getCanonicalPath());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return TestFileUtils.loadTestFiles(standardCases);
  }

}
