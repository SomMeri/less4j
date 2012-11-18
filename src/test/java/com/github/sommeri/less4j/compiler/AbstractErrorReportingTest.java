package com.github.sommeri.less4j.compiler;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
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

@RunWith(Parameterized.class)
public abstract class AbstractErrorReportingTest {

  private final File lessFile;
  private final File partialCssFile;
  private final File errorList;
  private final String testName;

  public AbstractErrorReportingTest(File lessFile, File partialCssFile, File errorList, String testName) {
    this.lessFile = lessFile;
    this.partialCssFile = partialCssFile;
    this.testName = testName;
    this.errorList = errorList;
  }

  @Test
  public final void compileAndCompare() throws Throwable {
    try {
      String less = IOUtils.toString(new FileReader(lessFile));
      LessCompiler compiler = getCompiler();
      CompilationResult actual = compiler.compile(less);

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

  private void assertCorrectErrors(Less4jException error) {
    //validate partial css
    assertEquals(lessFile.toString(), canonize(expectedPartial()), canonize(error.getPartialResult().getCss()));
    //validate errors and warnings
    String completeErrorReport = generateErrorReport(error);
    assertEquals(lessFile.toString(), canonize(expectedErrors()), canonize(completeErrorReport));
  }

  private void assertCorrectWarnings(CompilationResult actual) {
    //validate css
    assertEquals(lessFile.toString(), canonize(expectedPartial()), canonize(actual.getCss()));
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

  private String expectedPartial() {
    try {
      return IOUtils.toString(new FileReader(partialCssFile));
    } catch (Throwable ex) {
      throw new RuntimeException(testName + " " + ex.getMessage(), ex);
    }
  }

  private String expectedErrors() {
    try {
      return IOUtils.toString(new FileReader(errorList));
    } catch (Throwable ex) {
      throw new RuntimeException(testName + " " + ex.getMessage(), ex);
    }
  }

  private String generateErrorReport(Less4jException error) {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    CommandLinePrint printer = new CommandLinePrint(new PrintStream(outContent), new PrintStream(errContent));
    printer.reportErrors(error, "testCase");
    
    String completeErrorReport = errContent.toString();
    return completeErrorReport;
  }

  private String generateWarningsReport(CompilationResult result) {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    CommandLinePrint printer = new CommandLinePrint(new PrintStream(outContent), new PrintStream(errContent));
    printer.printWarnings("testCase", result);
    
    String completeErrorReport = errContent.toString();
    return completeErrorReport;
  }

}
