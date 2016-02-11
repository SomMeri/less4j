package com.github.sommeri.less4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.LessCompiler.Configuration;

/**
 * The test reproduces test files found in original less.js implementation. As
 * less.js has only only one tag and that tag is one year old, we took testsque
 * from the master branch.
 * 
 */
@Ignore
@RunWith(Parameterized.class)
public class SimpleCssTest extends AbstractFileBasedTest {

  private static final String inputLess = "src/test/resources/minitests/debug1.less";
  private static final String outputCss = "src/test/resources/minitests/debug1.css";

  private static final String mapdata = "src/test/resources/minitests/debug1.mapdata";
  private static final String config = "src/test/resources/minitests/debug1.config";

//  private static final String inputLess = "src/test/resources/minitests/import-reference-lessjs-issues.less";
//  private static final String outputCss = "src/test/resources/minitests/import-reference-lessjs-issues.css";


  public SimpleCssTest(File inputFile, File outputFile, File errorList, File mapdataFile, File configFile, String testName) {
    super(inputFile, outputFile, errorList, mapdataFile, configFile, testName);
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

  @Override
  protected Configuration createConfiguration(File cssOutput) {
    Configuration configuration = new Configuration();
    configuration.setCssResultLocation(new LessSource.FileSource(cssOutput));

    configuration.getSourceMapConfiguration().setInline(false);
    configuration.getSourceMapConfiguration().setLinkSourceMap(false);
    
    //configuration.addVariables(externalVariables());
    
    return configuration;
  }

  @Parameters(name = "Less: {5}")
  public static Collection<Object[]> allTestsParameters() {
    //justWait();
    Collection<Object[]> result = new ArrayList<Object[]>();
    result.add(new Object[] { new File(inputLess), new File(outputCss), null, new File(mapdata), new File(config), inputLess });
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
