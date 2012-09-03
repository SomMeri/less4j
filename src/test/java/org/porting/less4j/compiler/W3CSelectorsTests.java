package org.porting.less4j.compiler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.porting.less4j.AbstractFileBasedTest;
import org.porting.less4j.ILessCompiler;
import org.porting.less4j.core.CssPrinter;
import org.porting.less4j.debugutils.RhinoCompiler;

/**
 *  Testing whether less4j correctly compiles css3 selectors conforming to 
 *  <a href="http://www.w3.org/TR/css3-selectors/">w3c specification</a>. The test case
 *  goes through official w3c test cases downloaded from <a href="http://www.w3.org/Style/CSS/Test/CSS3/Selectors/current/">w3.org</a>. 
 *
 *  All tests have been run through less.js compiler. As less.js and less4j are not strictly identical,
 *  we modified less.js output from raw version into less4j compatible version. All this work is done
 *  in {@link RhinoCompiler} utility class.
 *  
 *  Special cases:
 *  1.) Less.js crashed on some tests, even as those tests contained valid css. Those stored in 
 *  less.js-incompatible directory. Expected outputs have been created manually. In general, less.js
 *  crashed on anything that contains :not(:nth-xxx-xxxx(an+b)) and :not(:lang(xxx)). TODO bug report
 *  
 *  Ignored cases:
 *  1.) Our compiler does not support namespaces yet, so those tests are not run yet. Tests on
 *  namespaces are in todo-namespaces and less.js-incompatible\namespaces directories. TODO this needs to be implemented
 *  
 *  2.) Some w3c tests test interpreter behavior on malformed css, those are irrelevant and 
 *  stored in incorrect-css and less.js-incompatible\incorrect-css directories.
 *
 */
@RunWith(Parameterized.class)
public class W3CSelectorsTests extends AbstractFileBasedTest {

  private static final String standardCases = "src\\test\\resources\\w3c-official-test-cases\\CSS3-Selectors\\";
  private static final String lessjsIncompatibleNegatedNth = "src\\test\\resources\\w3c-official-test-cases\\CSS3-Selectors\\less.js-incompatible\\correct-css\\negated-nth";
  private static final String lessjsIncompatibleNegatedVarious = "src\\test\\resources\\w3c-official-test-cases\\CSS3-Selectors\\less.js-incompatible\\correct-css\\negated-various";

  public W3CSelectorsTests(File inputFile, File outputFile, String testName) {
    super(inputFile, outputFile, testName);
  }

  //@Parameters(name="Compile Less: {0}, {2}")
  @Parameters()
  public static Collection<Object[]> allTestsParameters() {
    Collection<File> allFiles = FileUtils.listFiles(new File(standardCases), new String[] {"less"}, false);
    allFiles.addAll(FileUtils.listFiles(new File(lessjsIncompatibleNegatedVarious), new String[] {"less"}, false));
    allFiles.addAll(FileUtils.listFiles(new File(lessjsIncompatibleNegatedNth), new String[] {"less"}, false));
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
    String cssFileName = convertToOutputFilename(lessFileName);
    File cssFile = new File(cssFileName);
    return cssFile;
  }

  private static String convertToOutputFilename(String name) {
    if (name.endsWith(".less"))
      return name.substring(0, name.length() - 5) + ".css";
    
    return name;
  }

  protected ILessCompiler getCompiler() {
    return new CssPrinter();
  }

  protected String canonize(String text) {
    //ignore occasional end lines
    if (text.endsWith("\n"))
      return text.substring(0, text.length()-1);
    return text;
  }

}
