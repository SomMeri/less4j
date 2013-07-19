package com.github.sommeri.less4j.commandline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;

import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessCompiler.Problem;
import com.github.sommeri.less4j.utils.SourceMapValidator;
import com.github.sommeri.less4j.utils.TestFileUtils;
import com.github.sommeri.less4j.utils.debugonly.DebugAndTestPrint;

public abstract class CommandLineTest {


  protected static final String FILE_DOES_NOT_EXISTS = "Errors produced by compilation of src/test/resources/command-line/doesNotExists.less\nERROR The file " + DebugAndTestPrint.slashToplatformFileSeparator("src/test/resources/command-line/doesNotExists.less")+" does not exists.\nCould not compile the file src/test/resources/command-line/doesNotExists.less\n";

  protected static final String inputDir = "src/test/resources/command-line/";
  protected static final String customOutputDir = "src/test/resources/command-line/output";
  private static final String EXPECTED_CSS = ".test h4 {\n  declaration: ##ID;\n}\n";
  private static final String SOURCE_MAP_LINK = "/*# sourceMappingURL=##FILENAME */\n";
  private static final String ERRORS_CSS = "";
  private static final String WARNINGS_CSS = "{\n  padding: 2 2 2 2;\n}\n";
  //private static final String ERRORS_CSS = ".test h4 {\n  declaration: !#error#!;\n  padding: 2 2 2 2;\n}\n";

  protected SourceMapValidator sourceMapValidator = new SourceMapValidator();
  protected TestFileUtils fileUtils = new TestFileUtils();

  protected final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  protected final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private PrintStream originalOut = null;
  private PrintStream originalErr = null;

  @Before
  public void setUpStreams() {
    originalOut = System.out;
    originalErr = System.err;
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
  }

  @After
  public void cleanUpStreams() {
    System.setOut(originalOut);
    System.setErr(originalErr);
  }

  protected void assertHelpScreen() {
    String output = outContent.toString();
    assertTrue(output.contains("less4j test.less"));
  }

  protected void assertVersion() {
    String output = outContent.toString();
    assertTrue(output.contains("less4j " + CommandLine.getVersion()));
  }

  protected void assertMissingFilesError() {
    String output = outContent.toString();
    assertEquals("", output);

    String error = errContent.toString();
    assertEquals("Main parameters are required (\"[list of files]\")", error.trim());
  }

  protected void assertNoErrors() {
    String error = errContent.toString();
    assertEquals("", error);
  }

  protected void assertError(String expected) {
    String error = errContent.toString();
    assertEquals(expected, error.replace("\r\n", "\n"));
  }
  
  protected void assertErrorsAsInFile(String filename) {
    String expected = fileUtils.readFile(filename);
    String error = errContent.toString();
    assertEquals(expected, error.replace("\r\n", "\n"));
  }

  protected void assertSysoutAsInFile(String filename) {
    String expected = fileUtils.readFile(filename);
    String error = outContent.toString();
    assertEquals(expected, error.replace("\r\n", "\n"));
  }

  protected void assertSysout(String expected) {
    String out = outContent.toString();
    assertEquals(expected, out.replace("\r\n", "\n"));
  }

  protected void cleanErrors() {
    errContent.reset();
  }

  protected String correctCss(String id) {
    return EXPECTED_CSS.replace("##ID", id);
  }

  protected String correctCssWithSourceMap(String id, String filename) {
    String correctCss = correctCss(id);
    return correctCss + SOURCE_MAP_LINK.replace("##FILENAME", filename);
  }

  protected String incorrectCss() {
    return ERRORS_CSS;
  }
  
  protected String warningsCss() {
    return WARNINGS_CSS;
  }
  
  protected List<Problem> noProblems() {
    List<Problem> result = Collections.emptyList();
    return result;
  }

  protected void validateSourceMap(String mapdataFile, String cssFile, String mapFile) {
    CompilationResult result = new CompilationResult(fileUtils.readFile(cssFile), fileUtils.readFile(mapFile), noProblems());
    sourceMapValidator.validateSourceMap(result, new File(mapdataFile), new File(cssFile));
  }

}
