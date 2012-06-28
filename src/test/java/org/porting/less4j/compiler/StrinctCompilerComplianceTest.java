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

//FIXME: what does less.js do when the charset declaration goes AFTER ruleset? It is incorrect css anyway.
//if there is a difference I should at least document it
//TODO: document charset handling, it is easy enough to document
@RunWith(Parameterized.class)
public class StrinctCompilerComplianceTest extends AbstractFileBasedTest {

  private static final String inputDir = "src\\test\\resources\\compile-valid-css\\";
  private static final String outputDir = inputDir;

  public StrinctCompilerComplianceTest(File inputFile, File outputFile, String testName) {
    super(inputFile, outputFile, testName);
  }

  //TODO: the alternative annotation is going to be useful right after jUnit 11 comes out. It will contain
  //nicer test name.
  //@Parameters(name="Compile Less: {0}, {2}")
  @Parameters()
  public static Collection<Object[]> allTestsParameters() {
    Collection<File> allFiles = FileUtils.listFiles(new File(inputDir), new String[] {"less"}, false);
    Collection<Object[]> result = new ArrayList<Object[]>();
    for (File file : allFiles) {
      addFiles(result, file);
    }
//    addFiles(result, new File(inputDir + "comments.less"));

    return result;
  }

  private static void addFiles(Collection<Object[]> result, File... files) {
    for (File file : files) {
      result.add(new Object[] { file, findCorrespondingCss(file), file.getName() });
    }
  }

  protected static File findCorrespondingCss(File lessFile) {
    String lessFileName = lessFile.getName();
    String cssFileName = convertToOutputFilename(lessFileName);
    File cssFile = new File(outputDir + cssFileName);
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
    return text;
  }

}
