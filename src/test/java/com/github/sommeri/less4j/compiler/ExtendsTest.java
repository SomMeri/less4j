package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

//FIXME (!!!) add tests for values lists for attributes - e.g. error handling
public class ExtendsTest extends BasicFeaturesTest {

  private static final String standardCases = "src/test/resources/compile-basic-features/extends/";

  public ExtendsTest(File inputFile, File outputFile, File errorList, File mapdataFile, String testName) {
    super(inputFile, outputFile, errorList, mapdataFile, testName);
  }

  @Parameters(name="Less: {4}")
  public static Collection<Object[]> allTestsParameters() {
    return createTestFileUtils().loadTestFiles(standardCases);
  }

}
