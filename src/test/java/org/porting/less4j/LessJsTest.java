package org.porting.less4j;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
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
public class LessJsTest {

  private static final String inputLessDir = "src\\test\\resources\\less.js\\less\\";
  private static final String expectedCssDir = "src\\test\\resources\\less.js\\css\\";
  private final File inputFile;

  public LessJsTest(File inputFile, String testName) {
    this.inputFile = inputFile;
  }

  //TODO: the alternative annotation is going to be usefull right after jUnit 11 comes out. It will contain
  //nicer test name.
  //@Parameters(name="Compile Less: {0}, {2}")
  @Parameters()
  public static Collection<Object[]> allTestsParameters() {
    Collection<File> allFiles = FileUtils.listFiles(new File(inputLessDir), null, false);
    Collection<Object[]> result = new ArrayList<Object[]>();
    for (File file : allFiles) {
      result.add(new Object[] { file, file.getName() });
    }
    return result;
  }

  @Test
  public void runAllTests() throws Exception {
    File lessFile = inputFile;
    File cssFile = findCorrespondingCss(lessFile);
    testCompiler(lessFile, cssFile);
  }

  protected void testCompiler(File lessFile, File cssFile) throws Exception {
    String less = IOUtils.toString(new FileReader(lessFile));
    String css = IOUtils.toString(new FileReader(cssFile));

    ILessCompiler compiler = getCompiler();
    assertEquals("Incorrectly compiled the file " + lessFile, css, compiler.compile(less));
  }

  protected ILessCompiler getCompiler() {
    return new DummyLessCompiler();
  }

  private File findCorrespondingCss(File lessFile) {
    String lessFileName = lessFile.getName();
    String cssFileName = convertToOutputFilename(lessFileName);
    File cssFile = new File(expectedCssDir + cssFileName);
    return cssFile;
  }

  private String convertToOutputFilename(String name) {
    return name.substring(0, name.length() - 5) + ".css";
  }

}
