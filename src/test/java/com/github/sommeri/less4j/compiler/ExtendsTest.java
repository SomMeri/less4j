package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.LessCompiler.CompilationResult;

//FIXME (!!!) add tests for values lists for attributes - e.g. error handling
public class ExtendsTest extends BasicFeaturesTest {

  private static final String standardCases = "src/test/resources/compile-basic-features/extends/";
  private static final String extendAllMatch = "src/test/resources/compile-basic-features/extends/extend-all/match-replacement-by-element/";

  public ExtendsTest(File inputFile, File outputFile, File errorList, File mapdataFile, String testName) {
    super(inputFile, outputFile, errorList, mapdataFile, testName);
  }

  @Parameters(name="Less: {4}")
  public static Collection<Object[]> allTestsParameters() {
    //FIXME (!!!!) bwuhuhuuuuuu enable combied tests
    return createTestFileUtils().loadTestFiles(standardCases);
    //return createTestFileUtils().loadTestFiles(standardCases, extendAllMatch);
    //return createTestFileUtils().loadTestFiles(extendAllMatch);
  }

  protected void assertSourceMapValid(CompilationResult actual) {
    //FIXME (!!!!) enable source map test again!
  }

}
