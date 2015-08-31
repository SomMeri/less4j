package com.github.sommeri.less4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessCompiler.Configuration;
import com.github.sommeri.less4j.core.ThreadUnsafeLessCompiler;
import com.github.sommeri.less4j.core.TimeoutedLessCompiler;

/**
 * The test reproduces test files found in original less.js implementation. As
 * less.js has only only one tag and that tag is one year old, we took tests
 * from the master branch.
 * 
 */
@Ignore
@RunWith(Parameterized.class)
public class SimpleCssTest extends AbstractFileBasedTest {

//  private static final String inputLess = "src/test/resources/minitests/debug1.less";
  //private static final String inputLess = "c:/data/meri/less4java/srot/sppppppppppppppppppp-ample.less";
  //private static final String inputLess = "src/test/resources/minitests/javascript.less";
  private static final String outputCss = "src/test/resources/minitests/debug1.css";
  private static final String mapdata = "src/test/resources/minitests/debug1.mapdata";

//private static final String inputLess = "c://data//meri//less4java//bootstrap-3.0.2//bootstrap-3.0.2//less//theme.less";
//private static final String inputLess = "c://data//meri//less4java//bootstrap-3.2.0-less//less//bootstrap.less";
//private static final String inputLess = "c://data//meri//less4java//bootstrap-3.2.0-less//flamingo-m.less";
  
  private static final String inputLess = "c://data//meri//less4java//srot//semantic-ui//Semantic-UI-LESS//semantic.less";
//  private static final String inputLess = "c://data//meri//less4java//srot//semantic-ui//Semantic-UI-LESS//srot//multiple-test.less";

  //private static final String inputLess = "src/test/resources/minitests/bootstrap-debug.less";
  //private static final String inputLess = "src/test/resources/minitests/bootstrap-debug-2.less";
//  private static final String inputLess = "c://data//meri//less4java//bootstrap-3.0.2//bootstrap-3.0.2-zaloha//less//bootstrap.less";
//  private static final String printTo = "c://data//meri//less4java//workspace-juno-sr2//less4j-release-tests-working-dir//testTwitterBootstrap_3_0_0//less4j-compiled.css";
  //private static final String printTo = null;
  
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

//  protected CompilationResult compile(File lessFile, File cssOutput) throws Less4jException {
//    LessCompiler compiler = getCompiler();
//    Configuration configuration = createConfiguration(cssOutput);
//    
//    CustomLessSource source = new CustomLessSource(Arrays.asList("c:/data"), lessFile, "utf-8");
//    CompilationResult actual = compiler.compile(source, configuration);
//    //System.out.println(actual.getSourceMap());
//    return actual;
//  }
  
//  protected LessCompiler getCompiler() {
//    return new TimeoutedLessCompiler(1000, TimeUnit.MILLISECONDS);
//  }

  public static class CustomLessSource extends LessSource.FileSource {

    private final List<String> searchPaths;

    public CustomLessSource(List<String> searchPaths, File inputFile) {
      super(inputFile);
      this.searchPaths = searchPaths;
    }
    
    public CustomLessSource(List<String> searchPaths, File inputFile, String charsetName) {
      super(inputFile, charsetName);
      this.searchPaths = searchPaths;
    }

    public CustomLessSource(List<String> searchPaths, FileSource parent, String filename, String charsetName) {
      super(parent, filename, charsetName);
      this.searchPaths = searchPaths;
    }

    public CustomLessSource(List<String> searchPaths, FileSource parent, File inputFile, String charsetName) {
      super(parent, inputFile, charsetName);
      this.searchPaths = searchPaths;
    }

    /**
     * 
     * @param filename
     * @return
     */
    protected File createRelativeFile(String filename) {
      File thisFile = getInputFile();
      if (thisFile==null)
        return null;
      
      File thisDirectory = thisFile.getParentFile();
      File inputFile = new File(thisDirectory, filename);
      Iterator<String> cpIterator = searchPaths.iterator();
      while (!inputFile.exists() && cpIterator.hasNext()) {
        inputFile = new File(cpIterator.next(), filename);
      }
      
      return inputFile;
    }
    
    @Override
    public FileSource relativeSource(String filename) {
      return new CustomLessSource(searchPaths, this, createRelativeFile(filename), null);
    }
  }

//  @Override
//  protected Configuration createConfiguration(File cssOutput) {
//    Configuration configuration = super.createConfiguration(cssOutput);
//    configuration.getSourceMapConfiguration().setInline(false);
//    configuration.getSourceMapConfiguration().setLinkSourceMap(false);
//    configuration.getSourceMapConfiguration().setIncludeSourcesContent(true);
//    return configuration;
//  }

  @Override
  protected Configuration createConfiguration(File cssOutput) {
    Configuration configuration = new Configuration();
    configuration.setCssResultLocation(new LessSource.FileSource(cssOutput));

    configuration.getSourceMapConfiguration().setInline(false);
    configuration.getSourceMapConfiguration().setLinkSourceMap(false);
    
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
