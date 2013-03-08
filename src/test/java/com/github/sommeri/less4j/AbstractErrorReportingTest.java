package com.github.sommeri.less4j;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.IOUtils;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.commandline.CommandLinePrint;
import com.github.sommeri.less4j.core.ThreadUnsafeLessCompiler;
import com.github.sommeri.less4j.utils.DebugAndTestPrint;

@RunWith(Parameterized.class)
public abstract class AbstractErrorReportingTest {

  private final File lessFile;
  private final File cssOutput;
  private final File errorList;
  @SuppressWarnings("unused")
  private final String testName;

  public AbstractErrorReportingTest(File lessFile, File cssOutput, File errorList, String testName) {
    this.lessFile = lessFile;
    this.cssOutput = cssOutput;
    this.testName = testName;
    this.errorList = errorList;
  }

  @Test
  public final void compileAndCompare() throws Throwable {
    try {
      CompilationResult actual = compile(lessFile);
      assertCorrectWarnings(actual);
    } catch (ComparisonFailure ex) {
      ComparisonFailure fail = (ComparisonFailure) ex;
      throw new ComparisonFailure(fail.getMessage(), fail.getExpected(), fail.getActual());
    } catch (Less4jException ex) {
      printErrors(ex);
      assertCorrectErrors(ex);
    } catch (Throwable ex) {
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }

  protected void printErrors(Less4jException ex) {
  }

  protected CompilationResult compile(File lessFile) throws Less4jException, IOException {
    LessCompiler compiler = getCompiler();
    CompilationResult actual = compiler.compile(lessFile);
    return actual;
  }

  private void assertCorrectErrors(Less4jException error) {
    //validate errors and warnings
    String completeErrorReport = generateErrorReport(error);
    assertEquals(lessFile.toString(), canonize(expectedErrors()), canonize(completeErrorReport));
    //validate css
    assertEquals(lessFile.toString(), canonize(expectedCss()), canonize(error.getPartialResult().getCss()));
  }

  private void assertCorrectWarnings(CompilationResult actual) {
    //validate css
    String expectedCss = canonize(expectedCss());
    String actualCss = canonize(actual.getCss());
    assertEquals(expectedCss, actualCss);
    //validate warnings
    String completeErrorReport = generateWarningsReport(actual);
    assertEquals(canonize(expectedErrors()), canonize(completeErrorReport));
  }

  protected String canonize(String text) {
    //ignore end of line separator differences
    text = text.replace("\r\n", "\n");

    //ignore differences in various ways to write "1/1"
    text = text.replaceAll("1 */ *1", "1/1");

    //ignore occasional end lines
    while (text.endsWith("\n"))
      text=text.substring(0, text.length()-1);
    
    return text;
  }

  protected LessCompiler getCompiler() {
    return new ThreadUnsafeLessCompiler();
  }

  private String expectedCss() {
    try {
      return IOUtils.toString(new FileReader(cssOutput));
    } catch (Throwable ex) {
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }

  private String expectedErrors() {
    if (errorList==null || !errorList.exists())
      return "";
    
    try {
      return DebugAndTestPrint.platformFileSeparator(IOUtils.toString(new FileReader(errorList)));
    } catch (Throwable ex) {
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }

  protected String generateErrorReport(Less4jException error) {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    CommandLinePrint printer = new CommandLinePrint(new PrintStream(outContent), new PrintStream(errContent));
    printer.reportErrorsAndWarnings(error, "testCase", lessFile);
    
    String completeErrorReport = errContent.toString();
    return completeErrorReport;
  }

  private String generateWarningsReport(CompilationResult result) {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    CommandLinePrint printer = new CommandLinePrint(new PrintStream(outContent), new PrintStream(errContent));
    printer.printWarnings("testCase", lessFile, result);
    
    String completeErrorReport = errContent.toString();
    return completeErrorReport;
  }

}
