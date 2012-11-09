package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.utils.TestFileUtils;

//TODO: document css pasring issues and solutions
//FIXME: split appender into three: before after combined
//TODO: less.js can not pass this &<& It can pass & < & 
//TODO: document difference in the name+& handling of spaces (us: name + h1 less.js: name+h1)
//TODO: document difference in "h1 + {" handling (us: fail, less.js ignore that plus entirely)
public class NestedRulesetsTest extends BasicFeaturesTest {

  private static final String standardCases = "src/test/resources/compile-basic-features/nested-rulesets/";
  private static final String lessjs = "src/test/resources/compile-basic-features/nested-rulesets/less.js/";

  public NestedRulesetsTest(File inputFile, File outputFile, String testName) {
    super(inputFile, outputFile, testName);
  }

  @Parameters()
  public static Collection<Object[]> allTestsParameters() {
    return (new TestFileUtils()).loadTestFiles(standardCases, lessjs);
  }

}
