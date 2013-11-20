package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.LessCompiler.CompilationResult;

//FIXME (!!!) add tests for values lists for attributes - e.g. error handling
//FIXME !!!! documentation: element will attach itself to previous
//FIXME !!!! documentation: no partial class name match - not even for all
public class ExtendsTest extends BasicFeaturesTest {

  private static final String standardCases = "src/test/resources/compile-basic-features/extends/extend-full/";
  private static final String combinations = "src/test/resources/compile-basic-features/extends/combinations/";
  private static final String misc = "src/test/resources/compile-basic-features/extends/";
  private static final String extendAllMatch1 = "src/test/resources/compile-basic-features/extends/extend-all/match-replacement-by-element/";
  private static final String extendAllMatch2 = "src/test/resources/compile-basic-features/extends/extend-all/match-replacement-by-x/";
  private static final String extendAllMultiple = "src/test/resources/compile-basic-features/extends/extend-all/multiple-matches/";

  public ExtendsTest(File inputFile, File outputFile, File errorList, File mapdataFile, String testName) {
    super(inputFile, outputFile, errorList, mapdataFile, testName);
  }

  @Parameters(name="Less: {4}")
  public static Collection<Object[]> allTestsParameters() {
    //FIXME  !!!! bwuhuhuuuuuu enable combied tests
    //return createTestFileUtils().loadTestFiles(standardCases);
    //return createTestFileUtils().loadTestFiles(standardCases, extendAllMatch);
    return createTestFileUtils().loadTestFiles(misc, extendAllMatch1, extendAllMatch2, extendAllMultiple);
  }

  protected void assertSourceMapValid(CompilationResult actual) {
    //FIXME !!!! enable source map test again!
  }

}
