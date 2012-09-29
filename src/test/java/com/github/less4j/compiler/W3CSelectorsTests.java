package com.github.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.github.less4j.AbstractFileBasedTest;
import com.github.less4j.ILessCompiler;
import com.github.less4j.core.DefaultLessCompiler;
import com.github.less4j.debugutils.RhinoCompiler;
import com.github.less4j.utils.w3ctestsextractor.TestFileUtils;

/**
 *  Testing whether less4j correctly compiles css3 selectors conforming to 
 *  <a href="http://www.w3.org/TR/css3-selectors/">w3c specification</a>. The test case
 *  goes through official w3c test cases downloaded from <a href="http://www.w3.org/Style/CSS/Test/CSS3/Selectors/current/">w3.org</a>. 
 *
 *  All tests have been run through less.js compiler. As less.js and less4j are not strictly identical,
 *  we modified less.js output from raw version into less4j compatible version. All this work is done
 *  in {@link RhinoCompiler} utility class.
 *  
 *  Special cases:
 *  1.) Less.js crashed on some tests, even as those tests contained valid css. Those are stored in 
 *  less.js-incompatible directory. Expected outputs have been created manually. In general, less.js
 *  crashed on anything that contains :not(:nth-xxx-xxxx(an+b)) and :not(:lang(xxx)). 
 *  
 *  Ignored cases:
 *  1.) Our compiler does not support namespaces yet, so those tests are not run yet. Tests on
 *  namespaces are in todo-namespaces and less.js-incompatible\namespaces directories. 
 *  
 *  2.) Some w3c tests test interpreter behavior on malformed css, those are irrelevant and 
 *  stored in incorrect-css and less.js-incompatible\incorrect-css directories.
 *
 */
@RunWith(Parameterized.class)
public class W3CSelectorsTests extends AbstractFileBasedTest {

  private static final String standardCases = "src/test/resources/w3c-official-test-cases/CSS3-Selectors/";
  private static final String lessjsIncompatibleNegatedNth = "src/test/resources/w3c-official-test-cases/CSS3-Selectors/less.js-incompatible/correct-css/negated-nth";
  private static final String lessjsIncompatibleNegatedVarious = "src/test/resources/w3c-official-test-cases/CSS3-Selectors/less.js-incompatible/correct-css/negated-various";

  public W3CSelectorsTests(File inputFile, File outputFile, String testName) {
    super(inputFile, outputFile, testName);
  }

  //@Parameters(name="Compile Less: {0}, {2}")
  @Parameters()
  public static Collection<Object[]> allTestsParameters() {
    return TestFileUtils.loadTestFiles(standardCases, lessjsIncompatibleNegatedNth, lessjsIncompatibleNegatedVarious);
  }

  protected ILessCompiler getCompiler() {
    return new DefaultLessCompiler();
  }

  protected String canonize(String text) {
    //ignore occasional end lines
    if (text.endsWith("\n"))
      return text.substring(0, text.length()-1);
    return text;
  }

}
