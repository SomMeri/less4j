package com.github.sommeri.less4j.commandline;

import org.junit.Test;

import com.github.sommeri.less4j.utils.TestFileUtils;

public class MultiModeTest extends CommandLineTest {

  private TestFileUtils fileUtils = new TestFileUtils();
   
  @Test
  public void noInputFile() {
    CommandLine.main(new String[] {"-m"});
    assertMissingFilesError();
  }

  @Test
  public void oneInputFile() {
    String lessFile = inputDir+"one.less";
    String cssFile = inputDir+"one.css";
    fileUtils.removeFile(cssFile);
    CommandLine.main(new String[] {"-m", lessFile});
    fileUtils.assertFileContent(cssFile, expectedCss("one"));
    assertNoErrors();
  }

  @Test
  public void multipleInputFiles() {
    String multiLessFile1 = inputDir+"multi1.less";
    String multiCssFile1 = inputDir+"multi1.css";
    String multiLessFile2 = inputDir+"multi2.less";
    String multiCssFile2 = inputDir+"multi2.css";
    String multiLessFile3 = inputDir+"multi3.less";
    String multiCssFile3 = inputDir+"multi3.css";
    String wrongFile = inputDir+"doesNotExists.less";

    fileUtils.removeFiles(multiCssFile1, multiCssFile2, multiCssFile3);
    CommandLine.main(new String[] {"-m", multiLessFile1, wrongFile, multiLessFile2, multiLessFile3});
    fileUtils.assertFileContent(multiCssFile1, expectedCss("multi1"));
    fileUtils.assertFileContent(multiCssFile2, expectedCss("multi2"));
    fileUtils.assertFileContent(multiCssFile3, expectedCss("multi3"));
    assertError(FILE_DOES_NOT_EXISTS);
  }

  @Test
  public void customOutputDirectory() {
    String multiLessFile1 = inputDir+"multi1.less";
    String multiCssFile1 = customOutputDir+"/multi1.css";
    String multiLessFile2 = inputDir+"multi2.less";
    String multiCssFile2 = customOutputDir+"/multi2.css";
    String multiLessFile3 = inputDir+"multi3.less";
    String multiCssFile3 = customOutputDir+"/multi3.css";
    String wrongFile = inputDir+"doesNotExists.less";

    customOutputTest(customOutputDir, multiLessFile1, multiCssFile1, multiLessFile2, multiCssFile2, multiLessFile3, multiCssFile3, wrongFile);
    customOutputTest(customOutputDir + "/", multiLessFile1, multiCssFile1, multiLessFile2, multiCssFile2, multiLessFile3, multiCssFile3, wrongFile);
  }

  private void customOutputTest(String customOutputDir, String multiLessFile1, String multiCssFile1, String multiLessFile2, String multiCssFile2, String multiLessFile3, String multiCssFile3, String wrongFile) {
    fileUtils.removeFiles(multiCssFile1, multiCssFile2, multiCssFile3);
    CommandLine.main(new String[] {"-m", "-o", customOutputDir, multiLessFile1, wrongFile, multiLessFile2, multiLessFile3});
    fileUtils.assertFileContent(multiCssFile1, expectedCss("multi1"));
    fileUtils.assertFileContent(multiCssFile2, expectedCss("multi2"));
    fileUtils.assertFileContent(multiCssFile3, expectedCss("multi3"));
    assertError(FILE_DOES_NOT_EXISTS);
    cleanErrors();
  }

  @Test
  public void needToCreateOutputDirectory() {
    String newDir = inputDir + "doesNotExists/";
    String lessFile = inputDir+"one.less";
    String cssFile = newDir+"one.css";
    fileUtils.removeFile(newDir);
    CommandLine.main(new String[] {"--multiMode", "--outputDir", newDir, lessFile});
    fileUtils.assertFileContent(cssFile, expectedCss("one"));
    assertNoErrors();
  }

}
