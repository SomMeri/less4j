package com.github.sommeri.less4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * The test reproduces test files found in original less.js implementation. As
 * less.js has only only one tag and that tag is one year old, we took tests
 * from the master branch.
 * 
 */
@Ignore
@RunWith(Parameterized.class)
public class SimpleCssTest extends AbstractFileBasedTest {

//  private static final String inputLess = "src/test/resources/minitests/debug.less";
//  private static final String outputCss = "src/test/resources/minitests/debug.css";

//  private static final String inputLess = "src/test/resources/minitests/debug1.less";
//  private static final String outputCss = "src/test/resources/minitests/debug1.css";

  // ***********************************************************************
  // *** fail because of identifiers, functions and missing commas
  //    private static final String inputLess = "src/test/resources/minitests/mixins-guards.less";
  //    private static final String outputCss = "src/test/resources/minitests/mixins-guards.css";

  // *** fail because of wrong mixins reference - does not accept #mixin see debug.less
    private static final String inputLess = "src/test/resources/minitests/mixins.less";
    private static final String outputCss = "src/test/resources/minitests/mixins.css";

  // ***********************************************************************
  // *** work as they are
  // private static final String inputLess = "src/test/resources/minitests/mixins-named-args.less";
  // private static final String outputCss = "src/test/resources/minitests/mixins-named-args.css";

  //  private static final String inputLess = "src/test/resources/minitests/mixins-nested.less"; 
  //  private static final String outputCss = "src/test/resources/minitests/mixins-nested.css";

  //  private static final String inputLess = "src/test/resources/minitests/mixins-important.less";
  //  private static final String outputCss = "src/test/resources/minitests/mixins-important.css";

  //  private static final String inputLess = "src/test/resources/minitests/mixins-pattern.less";
  //  private static final String outputCss = "src/test/resources/minitests/mixins-pattern.css";

 //  private static final String inputLess = "src/test/resources/minitests/mixins-closure.less";
 //  private static final String outputCss = "src/test/resources/minitests/mixins-closure.css";

  // *** work with minor modification
  //private static final String inputLess = "src/test/resources/minitests/mixins-args.less";
  //private static final String outputCss = "src/test/resources/minitests/mixins-args.css";
  // ***********************************************************************
  // *** fail but it is OK - not implemented in less-1.3.0.js

  public SimpleCssTest(File inputFile, File cssFile, String testName) {
    super(inputFile, cssFile, testName);
  }

  @Parameters()
  public static Collection<Object[]> allTestsParameters() {
    Collection<Object[]> result = new ArrayList<Object[]>();
    result.add(new Object[] { new File(inputLess), new File(outputCss), inputLess });
    return result;
  }

  protected String canonize(String text) {
    return text.replaceAll("\r\n", "\n").replaceAll("#ffff00", "yellow").replaceAll("#ff0000", "red").replaceAll("#0000ff", "blue").replaceAll("! important", "!important").replaceAll("%!important", "% !important");
  }
}
