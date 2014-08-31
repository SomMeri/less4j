package com.github.sommeri.less4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessCompiler.Configuration;

/**
 * The test reproduces test files found in original less.js implementation. As
 * less.js has only only one tag and that tag is one year old, we took tests
 * from the master branch.
 * 
 */
@Ignore
@RunWith(Parameterized.class)
public class SimpleCssTest extends AbstractFileBasedTest {

  //private static final String inputLess = "src/test/resources/minitests/debug1.less";
  private static final String inputLess = "c:/data/meri/less4java/srot/sample.less";
  //private static final String inputLess = "src/test/resources/minitests/javascript.less";
  private static final String outputCss = "src/test/resources/minitests/debug1.css";
  private static final String mapdata = "src/test/resources/minitests/debug1.mapdata";

  //private static final String inputLess = "src/test/resources/minitests/bootstrap-debug.less";
  //private static final String inputLess = "src/test/resources/minitests/bootstrap-debug-2.less";
//  private static final String inputLess = "c://data//meri//less4java//bootstrap-3.0.2//bootstrap-3.0.2-zaloha//less//bootstrap.less";
//  private static final String printTo = "c://data//meri//less4java//workspace-juno-sr2//less4j-release-tests-working-dir//testTwitterBootstrap_3_0_0//less4j-compiled.css";
  private static final String printTo = null;
  
//  private static final String inputLess = "c://data//meri//less4java//slow-recursion//recursion//style.less"; 
//  private static final String printTo = null;

  //  private static final String outputCss = "src/test/resources/minitests/debug1.css";

  // ***********************************************************************
  // *** fail because of identifiers, functions and missing commas
  //    private static final String inputLess = "src/test/resources/minitests/mixins-guards.less";
  //    private static final String outputCss = "src/test/resources/minitests/mixins-guards.css";

  // *** fail because of wrong mixins reference - does not accept #mixin see debug.less
  //    private static final String inputLess = "src/test/resources/minitests/mixins.less";
  //    private static final String outputCss = "src/test/resources/minitests/mixins.css";

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

  public SimpleCssTest(File inputFile, File outputFile, File errorList, File mapdataFile, String testName) {
    super(inputFile, outputFile, errorList, mapdataFile, testName);
  }

//  @SuppressWarnings("unused")
//  protected CompilationResult supercompile(File lessFile, File cssOutput) throws Less4jException {
//    LessCompiler compiler = getCompiler();
//    Configuration configuration = createConfiguration(cssOutput);
//    configuration.setCssResultLocation((LessSource)null);
//    CompilationResult actual=null;
//    try {
//      String content = ".class {\n  margin: 1 1 1 1;\n}";//IOUtils.toString(new FileReader(lessFile));
//      String fullPath = "path/to/filename.less";
//      String filename = "filename.less";
////      LessSource.StringSource lessSource = new LessSource.StringSource(content, filename, new URI(fullPath));
//      LessSource.StringSource lessSource = new LessSource.StringSource(content);
//      actual = compiler.compile(lessSource, configuration);
//      
//      //System.out.println(actual.getSourceMap());
//    } catch (Throwable e) {
//      e.printStackTrace();
//    }
//    return actual;
//  }

  @Override
  protected CompilationResult compile(File lessFile, File cssOutput) throws Less4jException {
    CompilationResult result = super.compile(lessFile, cssOutput);
    System.out.println(result.getSourceMap());
    return result;
  }

  @Override
  protected Configuration createConfiguration(File cssOutput) {
    Configuration configuration = super.createConfiguration(cssOutput);
    configuration.getSourceMapConfiguration().setInline(false);
    configuration.getSourceMapConfiguration().setLinkSourceMap(true);
    configuration.getSourceMapConfiguration().setIncludeSourcesContent(true);
    return configuration;
  }

  @Parameters(name = "Less: {4}")
  public static Collection<Object[]> allTestsParameters() {
    //justWait();
    Collection<Object[]> result = new ArrayList<Object[]>();
    result.add(new Object[] { new File(inputLess), new File(outputCss), null, new File(mapdata), inputLess });
    return result;
  }

  @SuppressWarnings("unused")
  private static void justWait() {
    try {
      Thread.sleep(30000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  protected String canonize(String text) {
    return text.replaceAll("\r\n", "\n").replaceAll("#ffff00", "yellow").replaceAll("#ff0000", "red").replaceAll("#0000ff", "blue").replaceAll("! important", "!important").replaceAll("%!important", "% !important");
  }
}
