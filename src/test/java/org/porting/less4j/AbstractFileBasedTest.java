package org.porting.less4j;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileReader;

import org.apache.commons.io.IOUtils;
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

  public AbstractFileBasedTest(File lessFile, File cssFile, String testName) {
    this.lessFile = lessFile;
    this.cssFile = cssFile;
  }

  @Test
  public final void compileAndCompare() throws Exception {
    String less = IOUtils.toString(new FileReader(lessFile));
    String css = IOUtils.toString(new FileReader(cssFile));

    ILessCompiler compiler = getCompiler();
    assertEquals(lessFile.toString(), canonize(css), canonize(compiler.compile(less)));
  }

  private String canonize(String text) {
    return text.replaceAll("\\s", "");
  }

  protected abstract ILessCompiler getCompiler();

}
