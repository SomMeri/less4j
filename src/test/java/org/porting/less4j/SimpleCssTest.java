package org.porting.less4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.porting.less4j.core.DummyLessCompiler;

/**
 * The test reproduces test files found in original less.js implementation. As
 * less.js has only only one tag and that tag is one year old, we took tests
 * from the master branch.
 * 
 */
@RunWith(Parameterized.class)
public class SimpleCssTest extends AbstractFileBasedTest {

  private static final String inputLess = "src\\test\\resources\\less.js\\less\\css.less";
  private static final String outputCss = "src\\test\\resources\\less.js\\css\\css.css";

  public SimpleCssTest(File inputFile, File cssFile, String testName) {
    super(inputFile, cssFile, testName);
  }

  //TODO: the alternative annotation is going to be useful right after jUnit 11 comes out. It will contain
  //nicer test name.
  //@Parameters(name="Compile Less: {0}, {2}")
  @Parameters()
  public static Collection<Object[]> allTestsParameters() {
    Collection<Object[]> result = new ArrayList<Object[]>();
    result.add(new Object[] { new File(inputLess), new File(outputCss), inputLess });
    return result;
  }

  protected ILessCompiler getCompiler() {
    return new DummyLessCompiler();
  }

}
