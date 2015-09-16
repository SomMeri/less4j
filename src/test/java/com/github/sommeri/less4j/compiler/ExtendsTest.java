package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

//FIXME (!!!) add tests for values lists for attributes - e.g. error handling
//FIXME !!!! less.js documentation: element will attach itself to previous
//FIXME !!!! less.js documentation: no partial class name match - not even for all
//FIXME !!!! documentation - extend-combinators.less differences
public class ExtendsTest extends BasicFeaturesTest {

  private static final String standardCases = "src/test/resources/compile-basic-features/extends/";
  private static final String lessJsIncompatible = "src/test/resources/compile-basic-features/extends/less.js-incompatible/";
  private static final String extendFull = "src/test/resources/compile-basic-features/extends/extend-full/";
  //private static final String combinations = "src/test/resources/compile-basic-features/extends/combinations/";
  private static final String misc = "src/test/resources/compile-basic-features/extends/";
  private static final String extendAllCombinator = "src/test/resources/compile-basic-features/extends/extend-all/combinators/";
  private static final String extendAll = "src/test/resources/compile-basic-features/extends/extend-all/";
  private static final String extendAllEmbedded = "src/test/resources/compile-basic-features/extends/extend-all/embedded/";
  private static final String extendAllMatch1 = "src/test/resources/compile-basic-features/extends/extend-all/match-replacement-by-element/";
  private static final String extendAllMatch2 = "src/test/resources/compile-basic-features/extends/extend-all/match-replacement-by-x/";
  private static final String extendAllMultiple = "src/test/resources/compile-basic-features/extends/extend-all/multiple-matches/";

  public ExtendsTest(File inputFile, File outputFile, File errorList, File mapdataFile, File configFile, String testName) {
    super(inputFile, outputFile, errorList, mapdataFile, configFile, testName);
  }

  @Parameters(name="Less: {4}")
  public static Collection<Object[]> allTestsParameters() {
    return createTestFileUtils().loadTestFiles(standardCases, extendAllEmbedded, lessJsIncompatible, extendFull, extendAllCombinator, extendAll, misc, extendAllMatch1, extendAllMatch2, extendAllMultiple);
  }

}
