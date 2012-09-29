package com.github.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.github.less4j.AbstractFileBasedTest;
import com.github.less4j.ILessCompiler;
import com.github.less4j.core.CssPrinter;
import com.github.less4j.utils.w3ctestsextractor.TestFileUtils;

//FIXME: what does less.js do when the charset declaration goes AFTER ruleset? It is incorrect css anyway.
//if there is a difference I should at least document it
//FIXME: comments: many of my grammar rules throw away comments, semicolons and so on. I have to review them all and write test cases for everything.
@RunWith(Parameterized.class)
public class StrictComplianceTest extends AbstractFileBasedTest {

  private static final String inputDir = "src/test/resources/compile-valid-css/";

  public StrictComplianceTest(File inputFile, File outputFile, String testName) {
    super(inputFile, outputFile, testName);
  }

  //TODO: the alternative annotation is going to be useful right after jUnit 11 comes out. It will contain
  //nicer test name.
  //@Parameters(name="Compile Less: {0}, {2}")
  @Parameters()
  public static Collection<Object[]> allTestsParameters() {
    //return TestFileUtils.loadTestFile(inputDir, "nth-variants.less");
    return TestFileUtils.loadTestFiles(inputDir);
  }

  protected ILessCompiler getCompiler() {
    return new CssPrinter();
  }

  protected String canonize(String text) {
    return text;
  }

}
