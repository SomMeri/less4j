package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.AbstractFileBasedTest;

public class EscapingAndInterpolationTest extends AbstractFileBasedTest {

  private static final String standardCases = "src/test/resources/compile-basic-features/escaping/";

  public EscapingAndInterpolationTest(File inputFile, File outputFile, File errorList, File mapdataFile, File configFile, String testName) {
    super(inputFile, outputFile, errorList, mapdataFile, configFile, testName);
  }

  @Parameters(name="Less: {4}")
  public static Collection<Object[]> allTestsParameters() {
    return createTestFileUtils().loadTestFiles(standardCases);
  }

}
