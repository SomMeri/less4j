package com.github.sommeri.less4j.commandline;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;

public abstract class CommandLineTest {

  protected static final String FILE_DOES_NOT_EXISTS = "The file src/test/resources/command-line/doesNotExists.less does not exists.\nCould not compile the file src/test/resources/command-line/doesNotExists.less\n";
  protected static final String inputDir = "src/test/resources/command-line/";
  protected static final String customOutputDir = "src/test/resources/command-line/output";
  private static final String EXPECTED_CSS = ".test h4 {\n  declaration: ##ID;\n}\n";

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

  protected void assertSysout(String expected) {
    String out = outContent.toString();
    assertEquals(expected, out.replace("\r\n", "\n"));
  }

  protected void cleanErrors() {
    errContent.reset();
  }
  
  protected String expectedCss(String id) {
    return EXPECTED_CSS.replace("##ID", id);
  }

}
