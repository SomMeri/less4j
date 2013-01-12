package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.utils.TestFileUtils;

public class ImportsUnknownInputFileTest extends AbstractErrorReportingTest {

  private static final String standardCases = "src/test/resources/compile-basic-features/import/unknown-input-file/";

  public ImportsUnknownInputFileTest(File lessFile, File partialCssFile, File errorList, String testName) {
    super(lessFile, partialCssFile, errorList, testName);
  }

  @Parameters()
  public static Collection<Object[]> allTestsParameters() {
    return (new TestFileUtils(".err")).loadTestFiles(standardCases);
  }

  @Override
  protected CompilationResult compile(File lessFile) throws Less4jException, IOException {
    String less = IOUtils.toString(new FileReader(lessFile));
    LessCompiler compiler = getCompiler();
    CompilationResult actual = compiler.compile(less);
    return actual;
  }

}
