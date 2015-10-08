package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

public class ExternalVariablesTest extends BasicFeaturesTest {

  private static final String standardCases = "src/test/resources/external-variables/";

  public ExternalVariablesTest(File inputFile, File outputFile, File errorList, File mapdataFile, File configFile, String testName) {
    super(inputFile, outputFile, errorList, mapdataFile, configFile, testName);
  }

  @Parameters(name="Less: {5}")
  public static Collection<Object[]> allTestsParameters() {
    return createTestFileUtils().loadTestFiles(standardCases);
  }

  @Override
  protected String canonize(String text) {
    text = super.canonize(text);
    return text.replaceAll("parse-error-[0-9]", "parse-error-x");
  }

}
