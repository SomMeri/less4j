package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.AbstractFileBasedTest;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.core.DefaultLessCompiler;

/**
 *  Testing whether less4j correctly compiles css3 media conforming to 
 *  <a href="http://www.w3.org/TR/css3-mediaqueries/">w3c specification</a>. The test case
 *  goes through official w3c test cases extracted from <a href="http://www.w3.org/Style/CSS/Test/MediaQueries/20120229/">w3.org</a>.
 *
 *  As less.js does not support aspect-ratio in css3 media, we did not run inputs through it. Instead, we assume that the compiler 
 *  output must be the same as its input.
 *   
 *  Ignored cases:
 *  1.) Some w3c tests test interpreter behavior on malformed css, those are irrelevant and 
 *  stored in incorrect-css directory.
 *
 */
@RunWith(Parameterized.class)
public class W3CMediaTests extends AbstractFileBasedTest {

  private static final String standardCases = "src/test/resources/w3c-official-test-cases/CSS3-Media/";

  public W3CMediaTests(File inputFile, File outputFile, String testName) {
    super(inputFile, outputFile, testName);
  }

  //@Parameters(name="Compile Less: {0}, {2}")
  @Parameters()
  public static Collection<Object[]> allTestsParameters() {
    Collection<File> allFiles = FileUtils.listFiles(new File(standardCases), new String[] {"less"}, false);
    Collection<Object[]> result = new ArrayList<Object[]>();
    for (File file : allFiles) {
      addFiles(result, file);
    }
//    addFiles(result, new File(inputDir + "css3-modsel-144.less"));

    return result;
  }

  protected static void addFiles(Collection<Object[]> result, File... files) {
    for (File file : files) {
      result.add(new Object[] { file, findCorrespondingCss(file), file.getName() });
    }
  }

  protected static File findCorrespondingCss(File lessFile) {
    String lessFileName = lessFile.getPath();
    File cssFile = new File(lessFileName);
    return cssFile;
  }

  protected LessCompiler getCompiler() {
    return new DefaultLessCompiler();
  }

  protected String canonize(String text) {
    //ignore end of line separator differences
    text = text.replace("\r\n", "\n");

    //ignore differences in various ways to write "1/1"
    text = text.replaceAll("1 */ *1", "1/1");

    //ignore occasional end lines
    if (text.endsWith("\n"))
      return text.substring(0, text.length()-1);
    return text;
  }

}
