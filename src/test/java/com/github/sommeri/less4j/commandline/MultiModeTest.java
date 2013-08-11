package com.github.sommeri.less4j.commandline;

import org.junit.Test;

public class MultiModeTest extends CommandLineTest {

  @Test
  public void noInputFile() {
    CommandLine.main(new String[] {"-m"});
    assertMissingFilesError();
  }

  @Test
  public void oneInputFile() {
    String lessFile = inputDir+"one.less";
    String cssFile = inputDir+"one.css";
    String mapFile = inputDir+"one.css.map";
    fileUtils.removeFiles(cssFile, mapFile);
    CommandLine.main(new String[] {"-m", lessFile});
    fileUtils.assertFileContent(cssFile, correctCss("one"));
    fileUtils.assertFileNotExists(mapFile);
    assertNoErrors();
  }

  @Test
  public void multipleInputFiles() {
    String multiLessFile1 = inputDir+"multi1.less";
    String multiCssFile1 = inputDir+"multi1.css";
    String multiMapFile1 = inputDir+"multi1.map";
    String multiLessFile2 = inputDir+"multi2.less";
    String multiCssFile2 = inputDir+"multi2.css";
    String multiMapFile2 = inputDir+"multi2.map";
    String multiLessFile3 = inputDir+"multi3.less";
    String multiCssFile3 = inputDir+"multi3.css";
    String multiMapFile3 = inputDir+"multi3.map";
    String wrongFile = inputDir+"doesNotExists.less";

    fileUtils.removeFiles(multiCssFile1, multiCssFile2, multiCssFile3, multiMapFile1, multiMapFile2, multiMapFile3);
    CommandLine.main(new String[] {"-m", multiLessFile1, wrongFile, multiLessFile2, multiLessFile3});
    fileUtils.assertFileContent(multiCssFile1, correctCss("multi1"));
    fileUtils.assertFileNotExists(multiMapFile1);
    fileUtils.assertFileContent(multiCssFile2, correctCss("multi2"));
    fileUtils.assertFileNotExists(multiMapFile2);
    fileUtils.assertFileContent(multiCssFile3, correctCss("multi3"));
    fileUtils.assertFileNotExists(multiMapFile3);
    assertError(FILE_DOES_NOT_EXISTS);
  }

  @Test
  public void sourceMap() {
    String multiLessFile1 = inputDir+"multi1.less";
    String multiCssFile1 = inputDir+"multi1.css";
    String multiMapFile1 = inputDir+"multi1.css.map";
    String multiLessFile2 = inputDir+"multi2.less";
    String multiCssFile2 = inputDir+"multi2.css";
    String multiMapFile2 = inputDir+"multi2.css.map";
    
    String mapdataFile1 = inputDir+"multi1.mapdata";
    String mapdataFile2 = inputDir+"multi2.mapdata";


    fileUtils.removeFiles(multiCssFile1, multiCssFile2, multiMapFile1, multiMapFile2);
    CommandLine.main(new String[] {"-m", "--sourceMap", multiLessFile1, multiLessFile2});
    fileUtils.assertFileContent(multiCssFile1, correctCssWithSourceMap("multi1", "multi1.css.map"));
    validateSourceMap(mapdataFile1, multiCssFile1, multiMapFile1);
    fileUtils.assertFileContent(multiCssFile2, correctCssWithSourceMap("multi2", "multi2.css.map"));
    validateSourceMap(mapdataFile2, multiCssFile2, multiMapFile2);
    assertNoErrors();
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
    fileUtils.assertFileContent(multiCssFile1, correctCss("multi1"));
    fileUtils.assertFileContent(multiCssFile2, correctCss("multi2"));
    fileUtils.assertFileContent(multiCssFile3, correctCss("multi3"));
    assertError(FILE_DOES_NOT_EXISTS);
    cleanErrors();
  }

  @Test
  public void needToCreateOutputDirectory() {
    String newDir = inputDir + "doesNotExists/";
    String lessFile = inputDir+"one.less";
    String cssFile = newDir+"one.css";
    String mapFile = newDir+"one.map";
    fileUtils.removeFile(newDir);
    CommandLine.main(new String[] {"--multiMode", "--outputDir", newDir, lessFile});
    fileUtils.assertFileContent(cssFile, correctCss("one"));
    fileUtils.assertFileNotExists(mapFile);
    assertNoErrors();
  }

  @Test
  public void errorsAndWarnings() {
    String multiLessFile1 = inputDir+"errors.less";
    String multiCssFile1 = inputDir+"errors.css";
    String multiErrFile1 = inputDir+"errors.err";
    String multiLessFile2 = inputDir+"warnings.less";
    String multiCssFile2 = inputDir+"warnings.css";
    String multiErrFile2 = inputDir+"warnings.err";
    String multiLessFile3 = inputDir+"errorsandwarnings.less";
    String multiCssFile3 = inputDir+"errorsandwarnings.css";
    String multiErrFile3 = inputDir+"errorsandwarnings.err";

    fileUtils.removeFiles(multiCssFile1, multiCssFile2, multiCssFile3);
    CommandLine.main(new String[] {"-m", multiLessFile1, multiLessFile2, multiLessFile3});
    fileUtils.assertFileNotExists(multiCssFile1);
    fileUtils.assertFileContent(multiCssFile2, warningsCss());
    fileUtils.assertFileNotExists(multiCssFile3);
    assertError(fileUtils.concatenateFiles(multiErrFile1, multiErrFile2, multiErrFile3));
  }
  
  @Test
  public void partialOutputWithErrorsAndWarnings() {
    String multiLessFile1 = inputDir+"errors.less";
    String multiCssFile1 = inputDir+"errors.css";
    String multiErrFile1 = inputDir+"errors.err";
    String multiExpectedCssFile1 = inputDir+"errors.expcss";
    String multiLessFile2 = inputDir+"warnings.less";
    String multiCssFile2 = inputDir+"warnings.css";
    String multiErrFile2 = inputDir+"warnings.err";
    String multiLessFile3 = inputDir+"errorsandwarnings.less";
    String multiCssFile3 = inputDir+"errorsandwarnings.css";
    String multiErrFile3 = inputDir+"errorsandwarnings.err";
    String multiExpectedCssFile3 = inputDir+"errorsandwarnings.expcss";

    fileUtils.removeFiles(multiCssFile1, multiCssFile2, multiCssFile3);
    CommandLine.main(new String[] {"-m", "-pi", multiLessFile1, multiLessFile2, multiLessFile3});
    fileUtils.assertSameFileContent(multiExpectedCssFile1, multiCssFile1);
    fileUtils.assertFileContent(multiCssFile2, warningsCss());
    fileUtils.assertSameFileContent(multiExpectedCssFile3, multiCssFile3);
    assertError(fileUtils.concatenateFiles(multiErrFile1, multiErrFile2, multiErrFile3));
  }

}
