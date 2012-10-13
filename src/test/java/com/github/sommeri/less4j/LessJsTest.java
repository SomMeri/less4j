package com.github.sommeri.less4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
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
public class LessJsTest extends AbstractFileBasedTest {

  private static final String inputLessDir = "src/test/resources/less.js/less/";
  private static final String expectedCssDir = "src/test/resources/less.js/css/";

  public LessJsTest(File inputFile, File cssFile, String testName) {
    super(inputFile, cssFile, testName);
  }

  @Parameters()
  public static Collection<Object[]> allTestsParameters() {
    Collection<File> allFiles = FileUtils.listFiles(new File(inputLessDir), null, false);
    Collection<Object[]> result = new ArrayList<Object[]>();
    for (File file : allFiles) {
      result.add(new Object[] { file, findCorrespondingCss(file), file.getName() });
    }
    return result;
  }

  protected static File findCorrespondingCss(File lessFile) {
    String lessFileName = lessFile.getName();
    String cssFileName = convertToOutputFilename(lessFileName);
    File cssFile = new File(expectedCssDir + cssFileName);
    return cssFile;
  }

  private static String convertToOutputFilename(String name) {
    return name.substring(0, name.length() - 5) + ".css";
  }

}
