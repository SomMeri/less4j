package com.github.sommeri.less4j.antlr4.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.antlr4.Antlr4_AbstractFileBasedTest;

public class Antlr4_FunctionsTest extends Antlr4_AbstractFileBasedTest {

  private static final String standardCases = "src/test/resources/compile-basic-features/functions/";

  public Antlr4_FunctionsTest(File inputFile, File outputFile, File errorList, File mapdataFile, String testName) {
    super(inputFile, outputFile, errorList, mapdataFile, testName);
  }

  @Parameters(name="Less: {4}")
  public static Collection<Object[]> allTestsParameters() {
    return createTestFileUtils().loadTestFiles(standardCases);
  }

}
