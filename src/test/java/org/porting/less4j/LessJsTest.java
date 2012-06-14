package org.porting.less4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.porting.less4j.core.DummyLessCompiler;

/**
 * The test reproduces test files found in original less.js implementation. As
 * less.js has only only one tag and that tag is one year old, we took tests
 * from the master branch.
 * 
 */
public class LessJsTest {

  private static final String inputLessDir = "src\\test\\resources\\less\\";
  private static final String expectedCssDir = "src\\test\\resources\\css\\";

  //FIXME: each one of these should be a separate test, however this is manageable ugliness for the first prototype 
  @Test
  public void runAllTestsCount() throws Exception {
    int count = 0;
    Set<AssertionError> errors = new HashSet<AssertionError>();

    Iterator<File> it = FileUtils.iterateFiles(new File(inputLessDir), null, false);
    while (it.hasNext()) {
      count++;
      File lessFile = it.next();
      File cssFile = findCorrespondingCss(lessFile);
      try {
        testCompiler(lessFile, cssFile);
      } catch (AssertionError ex) {
        errors.add(ex);
      }
    }

    if (!errors.isEmpty()) {
      String message = "Failed " + errors.size() + " out of " + count + " test cases. ";
      fail(message);
    }
  }

  @Test
  public void runAllTests() throws Exception {
    Iterator<File> it = FileUtils.iterateFiles(new File(inputLessDir), null, false);
    while (it.hasNext()) {
      File lessFile = it.next();
      File cssFile = findCorrespondingCss(lessFile);
      // TODO it would be cool if each one of these would be a separate test
      // in gui and report
      testCompiler(lessFile, cssFile);
    }
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
