package com.github.sommeri.less4j.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class TestFileUtils {

  private String[] additionalDataFileSuffixes = new String[0];

  public TestFileUtils() {
  }

  public TestFileUtils(String... additionalDataFileSuffix) {
    this.additionalDataFileSuffixes = additionalDataFileSuffix;
  }

  public Collection<Object[]> loadTestFiles(String... directories) {
    Collection<File> allFiles = getAllLessFiles(directories);
    Collection<Object[]> result = new ArrayList<Object[]>();
    for (File file : allFiles) {
      addFiles(result, file);
    }
    return result;
  }

  private Collection<File> getAllLessFiles(String... directories) {
    Collection<File> allFiles = new LinkedList<File>();
    for (String directory : directories) {
      allFiles.addAll(FileUtils.listFiles(new File(directory), new String[] { "less" }, false));
    }
    return allFiles;
  }

  private void addFiles(Collection<Object[]> result, File... files) {
    for (File file : files) {
      result.add(createParameters(file));
    }
  }

  private Object[] createParameters(File file) {
    List<Object> parameters = new ArrayList<Object>();
    parameters.add(file);
    parameters.add(findCorrespondingExpected(file));
    for (String suffix : additionalDataFileSuffixes) {
      parameters.add(findCorrespondingAdditional(file, suffix));
    }

    parameters.add(file.getName());
    return parameters.toArray();
  }

  private File findCorrespondingExpected(File lessFile) {
    String lessFileName = lessFile.getPath();
    String cssFileName = convertToOutputFilename(lessFileName);
    File cssFile = new File(cssFileName);
    return cssFile;
  }

  private String convertToOutputFilename(String name) {
    if (name.endsWith(".less")) {
      return name.substring(0, name.length() - 5) + ".css";
    }

    return name;
  }

  private File findCorrespondingAdditional(File lessFile, String newSuffix) {
    String lessFileName = lessFile.getPath();
    String cssFileName = convertToAdditionalFilename(lessFileName, newSuffix);
    File cssFile = new File(cssFileName);
    return cssFile;
  }

  private String convertToAdditionalFilename(String name, String newSuffix) { 
    if (name.endsWith(".less")) {
      return name.substring(0, name.length() - 5) + newSuffix;
    }

    return name;
  }

  public Collection<Object[]> loadTestFile(String directory, String filename) {
    String name = directory + filename;
    File input = new File(name);
    Collection<Object[]> result = new ArrayList<Object[]>();
    addFiles(result, input);
    return result;
  }

  public void removeFile(String filename) {
    File file = new File(filename);
    if (file.exists())
      file.delete();
  }

  public void assertFileContent(String lessFile, String expectedContent) {
    String realContent = readFile(lessFile);
    assertEquals(expectedContent, realContent);
  }

  public void assertSameFileContent(String expectedFile, String actualFile) {
    String actualContent = readFile(actualFile);
    String expectedContent = readFile(expectedFile);
    assertEquals(expectedContent, actualContent);
  }

  public void assertFileNotExists(String filename) {
    File file = new File(filename);
    assertFalse("File " + filename + " should not exists.", file.exists());
  }

  public String readFile(String filename) {
    File inputFile = new File(filename);
    if (!inputFile.exists())
      fail("File " + filename + " was not created.");

    try {
      FileReader input = new FileReader(inputFile);
      String expected = IOUtils.toString(input).replace("\r\n", "\n");
      input.close();
      return expected;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public String concatenateFiles(String... files) {
    String result = "";
    for (String file : files) {
      result += readFile(file);
    }
    return result;
  }

  public void removeFiles(String... files) {
    for (String file : files) {
      removeFile(file);
    }
  }

}
