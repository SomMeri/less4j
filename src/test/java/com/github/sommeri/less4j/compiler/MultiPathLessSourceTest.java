package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.AbstractFileBasedTest;
import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessCompiler.Configuration;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.MultiPathFileSource;
import com.github.sommeri.less4j.MultiPathStringSource;

public class MultiPathLessSourceTest extends AbstractFileBasedTest {

  private static final String standardCases = "src/test/resources/compile-basic-features/include-path";
  private static final String includePath = "src/test/resources/compile-basic-features/import";
  private boolean useStringSource;

  public MultiPathLessSourceTest(File inputFile, File outputFile, File errorList, File mapdataFile, File configFile, String kind, String testName, boolean useStringSource) {
    super(inputFile, outputFile, errorList, mapdataFile, configFile, testName);
    this.useStringSource = useStringSource;
  }

  protected CompilationResult compile(File lessFile, File cssOutput) throws Less4jException {
    LessCompiler compiler = getCompiler();
    Configuration configuration = createConfiguration(cssOutput);
    LessSource source = createLessSource(lessFile);
    CompilationResult actual = compiler.compile(source, configuration);
    return actual;
  }

  private LessSource createLessSource(File lessFile) {
    return useStringSource? createStringSource(lessFile) : createFileSource(lessFile);
  }

  private LessSource createFileSource(File lessFile) {
    return new MultiPathFileSource(lessFile, new File (includePath));
  }

  private LessSource createStringSource(File lessFile) {
    String content = readFile(lessFile);
    return new MultiPathStringSource(content, "test", lessFile.toURI(), new File (includePath));
  }

  @Parameters(name="{4}: {5}")
  public static Collection<Object[]> allTestsParameters() {
    Collection<Object[]> loadedTestFiles = createTestFileUtils().loadTestFiles(standardCases);
    Collection<Object[]> result = new ArrayList<Object[]>(loadedTestFiles.size()*2);
    
    for (Object[] objects : loadedTestFiles) {
      Object[] useStringSourceArray = new Object[objects.length + 2];
      System.arraycopy(objects, 0, useStringSourceArray, 0, objects.length);
      useStringSourceArray[6] = useStringSourceArray[5];
      useStringSourceArray[5] = "String Based";
      useStringSourceArray[7] = true;
      result.add(useStringSourceArray);

      Object[] useFileSourceArray = new Object[objects.length + 2];
      System.arraycopy(objects, 0, useFileSourceArray, 0, objects.length);
      useFileSourceArray[6] = useFileSourceArray[5];
      useFileSourceArray[5] = "File Based";
      useFileSourceArray[7] = false;
      result.add(useFileSourceArray);
    }
    return result;
  }

}
