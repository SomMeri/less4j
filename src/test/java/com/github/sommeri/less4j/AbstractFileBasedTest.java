package com.github.sommeri.less4j;

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

import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.commandline.CommandLinePrint;
import com.github.sommeri.less4j.core.ThreadUnsafeLessCompiler;

/**
 * The test reproduces test files found in original less.js implementation. As
 * less.js has only only one tag and that tag is one year old, we took tests
 * from the master branch.
 * 
 */
@RunWith(Parameterized.class)
public abstract class AbstractFileBasedTest {

  private final File lessFile;
  private final File cssFile;
  private final String testName;

  public AbstractFileBasedTest(File lessFile, File cssFile, String testName) {
    this.lessFile = lessFile;
    this.cssFile = cssFile;
    this.testName = testName;
  }

  @Test
  public final void compileAndCompare() throws Throwable {
    try {
      String less = IOUtils.toString(new FileReader(lessFile));
      LessCompiler compiler = getCompiler();
      CompilationResult actual = compiler.compile(less);
      
      String expected = IOUtils.toString(new FileReader(cssFile));
      assertEquals(lessFile.toString(), canonize(expected), canonize(actual.getCss()));
    } catch (Less4jException ex) {
      String errorReport = generateErrorReport(ex);
      System.err.println(errorReport);
      throw new RuntimeException(testName + " " + errorReport, ex);
    } catch (Throwable ex) {
      if (ex instanceof ComparisonFailure) {
        ComparisonFailure fail = (ComparisonFailure)ex;
        throw new ComparisonFailure (testName + " " + fail.getMessage(), fail.getExpected(), fail.getActual());
      }
      throw new RuntimeException(testName + " " + ex.getMessage(), ex);
    }
  }

  protected abstract String canonize(String text);

  protected LessCompiler getCompiler() {
    return new ThreadUnsafeLessCompiler();
  }

  private String generateErrorReport(Less4jException error) {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    CommandLinePrint printer = new CommandLinePrint(new PrintStream(outContent), new PrintStream(errContent));
    printer.reportErrorsAndWarnings(error, testName);
    
    String completeErrorReport = errContent.toString();
    return completeErrorReport;
  }

}
