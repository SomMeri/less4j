package org.porting.less4j;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileReader;

import org.apache.commons.io.IOUtils;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

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
      ILessCompiler compiler = getCompiler();
      String actual = compiler.compile(less);
      
      String expected = IOUtils.toString(new FileReader(cssFile));
      assertEquals(lessFile.toString(), canonize(expected), canonize(actual));
      
    } catch (Throwable ex) {
      if (ex instanceof ComparisonFailure) {
        ComparisonFailure fail = (ComparisonFailure)ex;
        throw new ComparisonFailure (testName + " " + fail.getMessage(), fail.getExpected(), fail.getActual());
      }
      throw new RuntimeException(testName, ex);
    }
  }

  protected String canonize(String text) {
    return text.replaceAll("\\s", "");
  }

  protected abstract ILessCompiler getCompiler();

}
