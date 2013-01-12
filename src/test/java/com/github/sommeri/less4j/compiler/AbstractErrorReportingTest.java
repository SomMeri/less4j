package com.github.sommeri.less4j.compiler;

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
  private final String testName;
  protected boolean print = false;

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
      throw new ComparisonFailure(testName + " " + fail.getMessage(), fail.getExpected(), fail.getActual());
    } catch (Less4jException ex) {
      assertCorrectErrors(ex);
    } catch (Throwable ex) {
      throw new RuntimeException(testName + " " + ex.getMessage(), ex);
    }
  }

  protected CompilationResult compile(File lessFile) throws Less4jException, IOException {
    LessCompiler compiler = getCompiler();
    CompilationResult actual = compiler.compile(lessFile);
    return actual;
  }

  private void assertCorrectErrors(Less4jException error) {
    //validate css
    assertEquals(lessFile.toString(), canonize(expectedCss()), canonize(error.getPartialResult().getCss()));
    //validate errors and warnings
    String completeErrorReport = generateErrorReport(error);
    assertEquals(lessFile.toString(), canonize(expectedErrors()), canonize(completeErrorReport));
  }

  private void assertCorrectWarnings(CompilationResult actual) {
    //validate css
    String expectedCss = canonize(expectedCss());
    String actualCss = canonize(actual.getCss());
    if (print) {
    System.out.println("******** expected css **********");
    System.out.println(expectedCss);
    System.out.println("******** actual css **********");
    System.out.println(actualCss);
    }
    assertEquals(lessFile.toString(), expectedCss, actualCss);
    //validate warnings
    String completeErrorReport = generateWarningsReport(actual);
    assertEquals(lessFile.toString(), canonize(expectedErrors()), canonize(completeErrorReport));
  }

  protected String canonize(String text) {
    return text.replace("\r\n", "\n");
  }

  protected LessCompiler getCompiler() {
    return new ThreadUnsafeLessCompiler();
  }

  private String expectedCss() {
    try {
      return IOUtils.toString(new FileReader(cssOutput));
    } catch (Throwable ex) {
      throw new RuntimeException(testName + " " + ex.getMessage(), ex);
    }
  }

  private String expectedErrors() {
    if (errorList==null || !errorList.exists())
      return "";
    
    try {
      return DebugAndTestPrint.platformFileSeparator(IOUtils.toString(new FileReader(errorList)));
    } catch (Throwable ex) {
      throw new RuntimeException(testName + " " + ex.getMessage(), ex);
    }
  }

  private String generateErrorReport(Less4jException error) {
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
