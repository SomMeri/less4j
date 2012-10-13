package com.github.sommeri.less4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.core.DefaultLessCompiler;

/**
 * The test reproduces test files found in original less.js implementation. As
 * less.js has only only one tag and that tag is one year old, we took tests
 * from the master branch.
 * 
 */
//@Ignore
@RunWith(Parameterized.class)
public class SimpleCssTest extends AbstractFileBasedTest {


    private static final String inputLess = "src/test/resources/minitests/css/debug.less";
    private static final String outputCss = "src/test/resources/minitests/css/debug.css";

    // ***********************************************************************
    //fail because of pattern matching
//    private static final String inputLess = "src/test/resources/minitests/css/mixins-pattern.less";
//    private static final String outputCss = "src/test/resources/minitests/css/mixins-pattern.css";

  // closure feature is not implemented
  //  private static final String inputLess = "src/test/resources/minitests/css/mixins-closure.less";
  //  private static final String outputCss = "src/test/resources/minitests/css/mixins-closure.css";

  //fail because of pattern matching
  //  private static final String inputLess = "src/test/resources/minitests/css/mixins-important.less";
  //  private static final String outputCss = "src/test/resources/minitests/css/mixins-important.css";

  // *** fail because of identifiers
  //  private static final String inputLess = "src/test/resources/minitests/css/mixins-guards.less";
  //  private static final String outputCss = "src/test/resources/minitests/css/mixins-guards.css";

  // *** fail because of namespaces - those are not implemented
  //  private static final String inputLess = "src/test/resources/minitests/css/mixins.less";
  //  private static final String outputCss = "src/test/resources/minitests/css/mixins.css";

  // ***********************************************************************
  // *** work as they are
  //  private static final String inputLess = "src/test/resources/minitests/css/mixins-nested.less"; 
  //  private static final String outputCss = "src/test/resources/minitests/css/mixins-nested.css";

    // *** work with minor modification
    //private static final String inputLess = "src/test/resources/minitests/css/mixins-args.less";
    //private static final String outputCss = "src/test/resources/minitests/css/mixins-args.css";
  // ***********************************************************************
  // *** fail but it is OK - not implemented in less-1.3.0.js
  //private static final String inputLess = "src/test/resources/minitests/css/mixins-named-args.less";
  //private static final String outputCss = "src/test/resources/minitests/css/mixins-named-args.css";

  public SimpleCssTest(File inputFile, File cssFile, String testName) {
    super(inputFile, cssFile, testName);
  }

  @Parameters()
  public static Collection<Object[]> allTestsParameters() {
    Collection<Object[]> result = new ArrayList<Object[]>();
    result.add(new Object[] { new File(inputLess), new File(outputCss), inputLess });
    return result;
  }

  protected LessCompiler getCompiler() {
    return new DefaultLessCompiler();
  }

  protected String canonize(String text) {
    return text.replaceAll("\r\n", "\n").replaceAll("#ffff00", "yellow").replaceAll("#ff0000", "red").replaceAll("#0000ff", "blue").replaceAll("! important", "!important").replaceAll("%!important", "% !important");
  }
}
