package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.AbstractFileBasedTest;
import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessCompiler.Configuration;
import com.github.sommeri.less4j.LessSource.StringSource;;

public class ImportsUnknownInputFileTest extends AbstractFileBasedTest {

  private static final String standardCases = "src/test/resources/compile-basic-features/import/unknown-input-file/";

  public ImportsUnknownInputFileTest(File inputFile, File outputFile, File errorList, File mapdataFile, String testName) {
    super(inputFile, outputFile, errorList, mapdataFile, testName);
  }

  @Parameters(name = "Less: {4}")
  public static Collection<Object[]> allTestsParameters() {
    return createTestFileUtils().loadTestFiles(standardCases);
  }

  @Override
  protected CompilationResult compile(File lessFile, File cssOutput) throws Less4jException {
    try {
      String less = IOUtils.toString(new FileReader(lessFile));
      LessCompiler compiler = getCompiler();
      Configuration configuration = createConfiguration(cssOutput);
      CompilationResult actual = compiler.compile(new StringSource(less), configuration);
      return actual;
    } catch (IOException ex) {
      throw new RuntimeException("Can not read less file " + lessFile.getName(), ex);
    }
  }

}
