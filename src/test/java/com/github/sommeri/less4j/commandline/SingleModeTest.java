package com.github.sommeri.less4j.commandline;

import org.junit.Test;

import com.github.sommeri.less4j.utils.TestFileUtils;

public class SingleModeTest extends CommandLineTest {

  private TestFileUtils fileUtils = new TestFileUtils();
   
  @Test
  public void oneInputFile() {
    String lessFile = inputDir+"one.less";
    String cssFile = inputDir+"one.css";
    fileUtils.removeFile(cssFile);
    CommandLine.main(new String[] {lessFile});
    assertSysout(expectedCss("one"));
    assertNoErrors();
  }

  @Test
  public void twoInputFiles() {
    String lessFile = inputDir+"one.less";
    String cssFile = inputDir+"oneNew.css";
    fileUtils.removeFile(cssFile);
    CommandLine.main(new String[] {lessFile, cssFile});
    fileUtils.assertFileContent(cssFile, expectedCss("one"));
    assertNoErrors();
  }

  @Test
  public void inputFileDoesNotExists1() {
    String lessFile = inputDir+"doesNotExists.less";
    CommandLine.main(new String[] {lessFile});
    assertError(FILE_DOES_NOT_EXISTS);
  }

  @Test
  public void inputFileDoesNotExists2() {
    String lessFile = inputDir+"doesNotExists.less";
    String cssFile = inputDir+"doesNotExists.css";
    CommandLine.main(new String[] {lessFile, cssFile});
    assertError(FILE_DOES_NOT_EXISTS);
  }

}
