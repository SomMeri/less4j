package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.utils.w3ctestsextractor.TestFileUtils;

//TODO: document: a nested ruleset can not be nested inside a mixin - unless simple mixin. Then it is possible and inherited
//TODO: document mixin can be used before it was defined
//TODO: document all matching mixins are used
//TODO: when a new mixin of the same name is defined, upper scope mixins of the same name are suppressed. (not sure whether all or only perfectly matching)
//TODO: ^ nie je bug, je to feature. Mixins are look up in the current scope and only if not found in the outer scope. DOCUMENT + examples

//TODO document variables scope issue including issue filled to less.js https://github.com/cloudhead/less.js/issues/973
public class MixinsTest extends BasicFeaturesTests {
  
  private static final String standardCases = "src/test/resources/compile-basic-features/mixins/";
  private static final String lessjsIncompatible = "src/test/resources/compile-basic-features/mixins/lessjs-incompatible";

  public MixinsTest(File inputFile, File outputFile, String testName) {
    super(inputFile, outputFile, testName);
  }

  @Parameters()
  public static Collection<Object[]> allTestsParameters() {
    //return TestFileUtils.loadTestFile(standardCases+"todo/", "expressions-flying-minus.less");
    return (new TestFileUtils()).loadTestFiles(standardCases, lessjsIncompatible);
  }

}
