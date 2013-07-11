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
import com.github.sommeri.less4j.LessCompiler.Configuration;
import com.github.sommeri.less4j.commandline.CommandLinePrint;
import com.github.sommeri.less4j.core.ThreadUnsafeLessCompiler;
import com.github.sommeri.less4j.utils.SourceMapValidator;

/**
 * The test reproduces test files found in original less.js implementation. As
 * less.js has only only one tag and that tag is one year old, we took tests
 * from the master branch.
 * 
 */
//FIXME: source map: clean up properly, deprecated to generate warning
@Deprecated
@RunWith(Parameterized.class)
public abstract class SourceMapAbstractFileBasedTest {

  private final File lessFile;
  private final File cssFile;
  private final File mapdataFile;
  private final String testName;

  private final SourceMapValidator sourceMapValidation = new SourceMapValidator();

  public SourceMapAbstractFileBasedTest(File lessFile, File cssFile, File mapdataFile, String testName) {
    this.lessFile = lessFile;
    this.cssFile = cssFile;
    this.mapdataFile = mapdataFile;
    this.testName = testName;
  }

  @Test
  public final void compileAndCompare() throws Throwable {
    try {
      LessCompiler compiler = getCompiler();
      
      Configuration options = new Configuration();
      options.setCssResultLocation(new File(lessFile.getParentFile(), "cssFileName.css"));
      
      CompilationResult actual = compiler.compile(lessFile, options);
      System.out.println(actual.getSourceMap());

      String expected = IOUtils.toString(new FileReader(cssFile));
      assertEquals(lessFile.toString(), canonize(expected), canonize(actual.getCss()));
      
      sourceMapValidation.validateSourceMap(actual, mapdataFile, cssFile);
    } catch (Less4jException ex) {
      String errorReport = generateErrorReport(ex);
      System.err.println(errorReport);
      throw new RuntimeException(errorReport, ex);
    } catch (Throwable ex) {
      if (ex instanceof ComparisonFailure) {
        ComparisonFailure fail = (ComparisonFailure)ex;
        throw new ComparisonFailure (fail.getMessage(), fail.getExpected(), fail.getActual());
      }
      if (ex instanceof AssertionError) {
        AssertionError fail = (AssertionError)ex;
        throw new AssertionError (fail.getMessage(), fail);
      }
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }

  protected String canonize(String text) {
    text = text.replace("\r\n", "\n");
    //ignore occasional end lines
    if (text.endsWith("\n"))
      return text.substring(0, text.length()-1);
    return text;
  }

  protected LessCompiler getCompiler() {
    return new ThreadUnsafeLessCompiler();
  }

  private String generateErrorReport(Less4jException error) {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    CommandLinePrint printer = new CommandLinePrint(new PrintStream(outContent), new PrintStream(errContent));
    printer.printToSysout(error.getPartialResult(), testName, lessFile);
    printer.reportErrorsAndWarnings(error, testName, lessFile);
    
    String completeErrorReport = outContent.toString() + errContent.toString();
    return completeErrorReport;
  }

}
